package reports

import org.store.core.beans.Order
import org.store.core.beans.OrderStatus
import org.store.core.beans.User
import org.store.core.globals.SomeUtils
import org.store.core.utils.reports.ReportCell
import org.store.core.utils.reports.ReportFilterField
import org.store.core.utils.reports.ReportRow
import org.store.core.utils.reports.ReportStyle
import org.store.core.utils.reports.ReportTable

def getParameter(a, p) {
  def paramValue = a.getRequest().getParameter(p);
  return (paramValue != null && !"".equals(paramValue)) ? paramValue : "";
}

def getParameterArr(a, p) {
  def paramValueArr = a.getRequest().getParameterValues(p);
  return (paramValueArr != null) ? paramValueArr : null;
}

def getUserName(id) {
  def u = action.dao.get(User.class, id)
  return u.getFullName()
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

def getPaymentPercent(payment) {
  return SomeUtils.strToDouble(action.dao.getStorePropertyValue("salesComision", "MERCHANT_"+payment, "0"))
}

def objectToRow(order, lang) {
  return new ReportRow()
  .addCell(new ReportCell(order.idOrder, "normal-left"))
  .addCell(new ReportCell(order.createdDate, "normal-date"))
  .addCell(new ReportCell(order.status.getStatusName(lang), "normal-left"))
  .addCell(new ReportCell(order.user.email, "normal-left"))
  .addCell(new ReportCell(order.totalProducts, "normal-right"))
  .addCell(new ReportCell(order.totalFees, "normal-right"))
  .addCell(new ReportCell(order.totalTax, "normal-right"))
  .addCell(new ReportCell(order.totalShipping, "normal-right"))
  .addCell(new ReportCell(order.totalDiscountPromotion, "normal-right"))
  .addCell(new ReportCell(order.getTotal(), "normal-right"))
}

def getInfo() {
    return [name: action.getText("report.sales.name", "Vendedores"), description: action.getText("report.sales.description", "Report de vendedores")]
}

def getConfig() {
  StringBuffer buff = new StringBuffer()
  // rango de fechas
  buff.append(new ReportFilterField(ReportFilterField.TYPE_RANGE,"created_ini",getParameter(action, "created_ini"), action.getText("created.between", "Created between")).setName2("created_end").setValue2(getParameter(action, "created_end")).setLabel2(action.getText("and", " and ")).setCssClass("field date").toString())
  // revendedor
  def userList = action.dao.getAdminUsers();
  if (userList != null) {
    buff.append(
      new ReportFilterField(ReportFilterField.TYPE_SELECT, "admin", getParameter(action, "admin"), action.getText("report.admin.user", "Seller"))
        .setCssClass("field string-medium")
        .addOption("", action.getText("all.admin.users", "All sellers"))
        .addOptionsFromBeanList(userList, "idUser", "userId")
        .toString()
    );
  }

  // combo de levels
  def statuses = action.getOrderStatusList();
  if (statuses != null && statuses.size() > 1) {
    buff.append("<p class=\"clearfix\">");
    buff.append("<span class=\"label\">"+action.getText("order.status", "Order Status")+"</span>");
    statuses.each {
      buff.append("<label class=\"check\">");
      buff.append("<input type=\"checkbox\" name=\"status\" value=\""+ it.id +"\" />");
      buff.append(it.getStatusName(action.getDefaultLanguage()));
      buff.append("</label>");
    }
    buff.append("</p>")
  }
  return buff.toString()
}

def execute() {
  def lang = action.getDefaultLanguage()
  def rt = new ReportTable(action.getText("report.sales.name", "Vendedores"))
          .setPageSize(ReportTable.PAGE_LETTER_H)
          .addStyle("header", new ReportStyle("header").setBkgColor("333333").setTextColor("FFFFFF").setNoWrap(true))
          .addStyle("header-right", new ReportStyle("header").setBkgColor("333333").setTextColor("FFFFFF").setNoWrap(true).setTextAlign("right"))
          .addStyle("total", new ReportStyle("total").setBkgColor("EEEEEE").setTextColor("333333").setBorderTop("333333", "solid", 3).setNoWrap(true).setTextWeight("bold").setTextSize(12))
          .addStyle("total-right", new ReportStyle("total-right").setBkgColor("EEEEEE").setTextColor("333333").setBorderTop("333333", "solid", 3).setNoWrap(true).setTextWeight("bold").setFormat("decimal").setTextAlign("right").setTextSize(11))
          .addStyle("vendedor", new ReportStyle("normal").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("left").setBorderBottom("333333", "solid", 1).setTextWeight("bold"))
          .addStyle("normal", new ReportStyle("normal").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("left").setBorderBottom("333333", "solid", 1))
          .addStyle("normal-right", new ReportStyle("normal-center").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("right").setBorderBottom("333333", "solid", 1).setFormat("decimal").setNoWrap(true))
          .addStyle("normal-date", new ReportStyle("normal-date").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("center").setBorderBottom("333333", "solid", 1).setFormat("date").setNoWrap(true))
          .addRow(new ReportRow()
            .addCell(new ReportCell("Order #", "header"))
            .addCell(new ReportCell("Created", "header"))
            .addCell(new ReportCell("Status", "header"))
            .addCell(new ReportCell("Customer", "header"))
            .addCell(new ReportCell("Payment", "header"))
            .addCell(new ReportCell("Total", "header-right"))
            .addCell(new ReportCell("Paid", "header-right"))
            .addCell(new ReportCell("Pending", "header-right"))
            .addCell(new ReportCell("Commission", "header-right"))
  )

  def colWidths = [54f,76f,80f,230f,144f,64f,64f,64f,64f]
  rt.setColWidths(colWidths)

  Date dateIni = SomeUtils.strToDate(getParameter(action, "created_ini"), lang);
  Date dateEnd = SomeUtils.strToDate(getParameter(action, "created_end"), lang);

  arrStatus = getParameterArr(action, "status")
  Long idAdmin = SomeUtils.strToLong(getParameter(action, "admin"));
  Integer maxRows = SomeUtils.strToInteger(getParameter(action, "maxrows"));

  String query = "select {t_order.*} from t_order left join t_user on t_user.idUser=t_order.user_idUser";

  String where = " where t_user.inventaryCode=:storeCode and t_order.idAdminUser is not null ";

  if (dateIni != null) {
    where += " and t_order.createdDate>=:dateIni ";
    rt.addFilter(action.getText("from.date", "From date"), getParameter(action, "created_ini"));
  }
  if (dateEnd != null) {
    where += " and t_order.createdDate<=:dateEnd ";
    rt.addFilter(action.getText("to.date", "To date"), getParameter(action, "created_end"));
  }

  if (arrStatus != null) {
    Long[] statuses = SomeUtils.strToLong(arrStatus)
    String idsStr = statuses.each {it.toString()}.join(",")
    where += " and t_order.status_id in ("+idsStr+") ";
    rt.addFilter(action.getText("order.status", "Order Status"), getStatusNames(statuses));
  }  else {
    where += " and t_order.status_id=0 ";
  }

  def orderBy = " order by ";
  if (idAdmin!=null) {
    where += " and t_order.idAdminUser=" + idAdmin.toString() + " ";
    rt.addFilter(action.getText("seller", "Seller"), (String) getUserName(getParameter(action, "admin")));
  } else {
    orderBy += " t_order.idAdminUser,";
  }
  orderBy += " t_order.idOrder";

  def sqlQuery = action.getDao().gethSession().createSQLQuery(query + where + orderBy).addEntity("t_order", Order.class);
  sqlQuery.setString("storeCode", action.getStoreCode());
  if (dateIni != null) sqlQuery.setDate("dateIni", SomeUtils.dateIni(dateIni));
  if (dateEnd != null) sqlQuery.setDate("dateEnd", SomeUtils.dateEnd(dateEnd));

  if (maxRows != null) sqlQuery.setMaxResults(maxRows);

  def t0 = 0, t1 = 0, t2 = 0, t3 = 0
  def oldAdmin = 0
  def numOrders = 0
  List<Object[]> list = sqlQuery.list();
  for (Object[] o: list) {
    def order = (Order) o;
    def adminPercent = getPaymentPercent(order.getPaymentMethod()) *  order.getTotal()

    if (o.idAdminUser!=oldAdmin) {
      if (numOrders>0) {
        // agregar total
        rt.addRow(
          new ReportRow()
            .addCell(new ReportCell(numOrders + " orders", "total",5))
            .addCell(new ReportCell(t0, "total-right"))
            .addCell(new ReportCell(t1, "total-right"))
            .addCell(new ReportCell(t2, "total-right"))
            .addCell(new ReportCell(t3, "total-right"))
        )
      }
      // encabezado
      if (idAdmin==null) rt.addRow(new ReportRow().addCell(new ReportCell(getUserName(o.idAdminUser), "vendedor", 9)))

      // inicializar para el siguiente vendedor
      t0 = 0
      t1 = 0
      t2 = 0
      t3 = 0
      numOrders = 0
      oldAdmin = o.idAdminUser
    }

    rt.addRow(new ReportRow()
        .addCell(new ReportCell(order.getIdOrder(), "normal"))
        .addCell(new ReportCell(order.getCreatedDate(), "normal-date"))
        .addCell(new ReportCell(order.getStatus().getStatusName(lang), "normal"))
        .addCell(new ReportCell(order.getUser().fullName, "normal"))
        .addCell(new ReportCell(order.getPaymentMethod(), "normal"))
        .addCell(new ReportCell(order.getTotal(), "normal-right"))
        .addCell(new ReportCell(order.getTotalPartialPaid(), "normal-right"))
        .addCell(new ReportCell(order.getTotalPartialPending(), "normal-right"))
        .addCell(new ReportCell(adminPercent, "normal-right"))
    )

    // sumar y contar
    t0 += order.getTotal()
    t1 += order.getTotalPartialPaid()
    t2 += order.getTotalPartialPending()
    t3 += adminPercent
    numOrders++
  }

  if (numOrders>0) {
    // agregar total
    rt.addRow(
      new ReportRow()
        .addCell(new ReportCell(numOrders + " orders", "total",5))
        .addCell(new ReportCell(t0, "total-right"))
        .addCell(new ReportCell(t1, "total-right"))
        .addCell(new ReportCell(t2, "total-right"))
        .addCell(new ReportCell(t3, "total-right"))
    )
  }

  return rt
}

if (input == "info") output = getInfo()
else if (input == "config") output = getConfig()
else if (input == "execute") output = execute()
else output = ""
