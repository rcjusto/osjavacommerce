package reports

import org.store.core.globals.SomeUtils
import org.store.core.utils.reports.ReportCell
import org.store.core.utils.reports.ReportFilterField
import org.store.core.utils.reports.ReportRow
import org.store.core.utils.reports.ReportStyle
import org.store.core.utils.reports.ReportTable
import org.store.core.beans.Order
import org.store.core.beans.OrderStatus

def getParameter(a, p) {
  def paramValue = a.getRequest().getParameter(p);
  return (paramValue != null && !"".equals(paramValue)) ? paramValue : "";
}

def getParameterArr(a, p) {
  def paramValueArr = a.getRequest().getParameterValues(p);
  return (paramValueArr != null) ? paramValueArr : null;
}

def getStatusNames(ids) {
  def buff = new StringBuilder()
  ids.each {
    def os = action.dao.get(OrderStatus.class, it)
    if (os!=null) {
      if (buff.toString()!="") buff.append(", ")
      buff.append(os.getStatusName(action.getDefaultLanguage()))
    }
  }
  return buff.toString()
}

def getInfo() {
    return [name: action.getText("report.profit.name", "Ganancias"), description: action.getText("report.profit.description", "Ganancias por orden")]
}

def getConfig() {
  StringBuffer buff = new StringBuffer()
  // rango de fechas
  buff.append(new ReportFilterField(ReportFilterField.TYPE_RANGE,"created_ini",getParameter(action, "created_ini"), action.getText("created.between", "Created between")).setName2("created_end").setValue2(getParameter(action, "created_end")).setLabel2(action.getText("and", " and ")).setCssClass("field date").toString())
  return buff.toString()
}

def execute() {
  def lang = action.getDefaultLanguage()
  def rt = new ReportTable(action.getText("report.profit.name", "Ganancias"))
          .setPageSize(ReportTable.PAGE_LETTER_H)
          .addStyle("header", new ReportStyle("header").setBkgColor("333333").setTextColor("FFFFFF").setNoWrap(true))
          .addStyle("header-right", new ReportStyle("header").setBkgColor("333333").setTextColor("FFFFFF").setNoWrap(true).setTextAlign("right"))
          .addStyle("total", new ReportStyle("total").setBkgColor("EEEEEE").setTextColor("333333").setBorderTop("333333", "solid", 3).setNoWrap(true).setTextWeight("bold").setTextSize(12))
          .addStyle("total-right", new ReportStyle("total-right").setBkgColor("EEEEEE").setTextColor("333333").setBorderTop("333333", "solid", 3).setNoWrap(true).setTextWeight("bold").setFormat("decimal").setTextAlign("right").setTextSize(11))
          .addStyle("normal-left", new ReportStyle("normal-left").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("left").setBorderBottom("333333", "solid", 1))
          .addStyle("normal-right", new ReportStyle("normal-center").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("right").setBorderBottom("333333", "solid", 1).setFormat("decimal").setNoWrap(true))
          .addStyle("normal-date", new ReportStyle("normal-center").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("center").setBorderBottom("333333", "solid", 1).setFormat("date").setNoWrap(true))
          .addRow(new ReportRow()
            .addCell(new ReportCell("Order #", "header"))
            .addCell(new ReportCell("Created", "header"))
            .addCell(new ReportCell("Customer", "header"))
            .addCell(new ReportCell("Company", "header"))
            .addCell(new ReportCell("Total", "header-right"))
            .addCell(new ReportCell("Product", "header-right"))
            .addCell(new ReportCell("Fees", "header-right"))
            .addCell(new ReportCell("Taxes", "header-right"))
            .addCell(new ReportCell("Shipping", "header-right"))
            .addCell(new ReportCell("Handling", "header-right"))
            .addCell(new ReportCell("Profit", "header-right"))
  )

  def colWidths = [54f,76f,230f,230f,64f,64f,64f,64f,64f,64f,64f]
  rt.setColWidths(colWidths)

  Date dateIni = SomeUtils.strToDate(getParameter(action, "created_ini"), lang);
  Date dateEnd = SomeUtils.strToDate(getParameter(action, "created_end"), lang);

  arrStatus = getParameterArr(action, "status")
  Integer maxRows = SomeUtils.strToInteger(getParameter(action, "maxrows"));

  String query = "select {t_order.*} from t_order left join t_user on t_user.idUser=t_order.user_idUser left join t_order_status on t_order_status.id=t_order.status_id ";

  String where = " where t_user.inventaryCode=:storeCode and t_order_status.statusType = 'approved' ";

  if (dateIni != null) {
    where += " and t_order.createdDate>=:dateIni ";
    rt.addFilter(action.getText("from.date", "From date"), getParameter(action, "created_ini"));
  }
  if (dateEnd != null) {
    where += " and t_order.createdDate<=:dateEnd ";
    rt.addFilter(action.getText("to.date", "To date"), getParameter(action, "created_end"));
  }

  def orderBy = " order by t_order.idOrder";

  def sqlQuery = action.getDao().gethSession().createSQLQuery(query + where + orderBy).addEntity("t_order", Order.class);
  sqlQuery.setString("storeCode", action.getStoreCode());
  if (dateIni != null) sqlQuery.setDate("dateIni", SomeUtils.dateIni(dateIni));
  if (dateEnd != null) sqlQuery.setDate("dateEnd", SomeUtils.dateEnd(dateEnd));

  if (maxRows != null) sqlQuery.setMaxResults(maxRows);

  def t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0, t6 = 0
  List<Object[]> list = sqlQuery.list();
  for (Object[] o: list) {
    def order = (Order) o;
    def profit = order.getTotal() - order.getCostProduct() - order.totalShipping;
    if (order.totalFees!=null) profit -= order.totalFees
    if (order.totalTax!=null) profit -= order.totalTax
    if (order.handlingCost!=null) profit -= order.handlingCost

    rt.addRow(
      new ReportRow()
      .addCell(new ReportCell(order.idOrder, "normal-left"))
      .addCell(new ReportCell(order.createdDate, "normal-date"))
      .addCell(new ReportCell(order.user.email, "normal-left"))
      .addCell(new ReportCell(order.user.companyName, "normal-left"))
      .addCell(new ReportCell(order.getTotal(), "normal-right"))
      .addCell(new ReportCell(order.getCostProduct(), "normal-right"))
      .addCell(new ReportCell((order.totalFees!=null) ? order.totalFees : 0, "normal-right"))
      .addCell(new ReportCell((order.totalTax!=null) ? order.totalTax : 0, "normal-right"))
      .addCell(new ReportCell(order.totalShipping, "normal-right"))
      .addCell(new ReportCell((order.handlingCost!=null) ? order.handlingCost : 0, "normal-right"))
      .addCell(new ReportCell(profit, "normal-right"))
    )

    // sumar totales
    t0 += order.getTotal()
    t1 += order.getCostProduct()
    if (order.totalFees!=null) t2 += order.totalFees
    if (order.totalTax!=null) t3 += order.totalTax
    t4 += order.totalShipping
    if (order.handlingCost!=null) t5 += order.handlingCost
    t6 += profit
  }
  // agregar total
  rt.addRow(
    new ReportRow()
        .addCell(new ReportCell("TOTAL", "total",4))
        .addCell(new ReportCell(t0, "total-right"))
        .addCell(new ReportCell(t1, "total-right"))
        .addCell(new ReportCell(t2, "total-right"))
        .addCell(new ReportCell(t3, "total-right"))
        .addCell(new ReportCell(t4, "total-right"))
        .addCell(new ReportCell(t5, "total-right"))
        .addCell(new ReportCell(t6, "total-right"))
  )

  return rt
}

if (input == "info") output = getInfo()
else if (input == "config") output = getConfig()
else if (input == "execute") output = execute()
else output = ""
