import org.store.core.beans.User
import org.store.core.beans.UserLevel
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

def levelRow(levelName) {
  return new ReportRow().addCell(new ReportCell(levelName, "level", 6));
}

def objectToRow(o) {
  def u = (User) o[0];
  return new ReportRow()
    .addCell(new ReportCell(u.getIdUser(), "normal-center"))
    .addCell(new ReportCell(u.getFullName(), "normal-left"))
    .addCell(new ReportCell(u.getCompanyName(), "normal-left"))
    .addCell(new ReportCell(u.getVisits(), "normal-center"))
    .addCell(new ReportCell(u.getRegisterDate(), "normal-date"))
    .addCell(new ReportCell(o[1], "normal-center"));
}

def getUserLevelName(Long level) {
    if (level == null) return "";
    def list = action.getUserLevelList();
    for (UserLevel userLevel : list)
        if (level.equals(userLevel.getId())) return userLevel.getName(action.getDefaultLanguage());
    return level.toString();
}


def getInfo() {
    return [name: action.getText("report.customers.name", "Clientes"), description: action.getText("report.customers.description", "Listado de clientes registrados en el sitio")]
}

def getConfig() {
  StringBuffer buff = new StringBuffer()
  // rango de fechas
  buff.append(
          new ReportFilterField(ReportFilterField.TYPE_RANGE,"registered_ini",getParameter(action, "registered_ini"), action.getText("registered.between", "Registered between")).setName2("registered_end").setValue2(getParameter(action, "registered_end")).setLabel2(action.getText("and", " and ")).setCssClass("field date").toString())
  // combo de levels
  def userLevels = action.getUserLevelList();
  if (userLevels != null && userLevels.size() > 1) {
    buff.append(
      new ReportFilterField(ReportFilterField.TYPE_SELECT, "level", getParameter(action, "level"), action.getText("report.customer.level", "Level"))
        .setCssClass("field string-medium")
        .addOption("", action.getText("all.customers", "All customers"))
        .addOptionsFromBeanList(userLevels, "id", "getName('" + action.getDefaultLanguage() + "')")
        .toString()
    );
  }
  // como de activos o bloqueados
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "active", getParameter(action, "active"), action.getText("report.customer.access", "Access")).setCssClass("field string-medium").addOption("", action.getText("all.customers", "All customers")).addOption("active", action.getText("only.active", "Only actives")).addOption("block", action.getText("only.block", "Only blocked")).toString());
  // checkbox de tax
  buff.append(new ReportFilterField(ReportFilterField.TYPE_CHECKBOX, "taxExempt", getParameter(action, "taxExempt")).setLabel2(action.getText("tax.exempt", "Tax Exempt")).setValue2("Y").toString());
  // checkbox de fees
  buff.append(new ReportFilterField(ReportFilterField.TYPE_CHECKBOX, "feeExempt", getParameter(action, "feeExempt")).setLabel2(action.getText("fee.exempt", "Fee Exempt")).setValue2("Y").toString());
  return buff.toString()
}

def execute() {
  def rt = new ReportTable(action.getText("report.customers.name", "Clientes"))
          .addStyle("header", new ReportStyle("header").setBkgColor("333333").setTextColor("FFFFFF"))
          .addStyle("header-center", new ReportStyle("header").setBkgColor("333333").setTextColor("FFFFFF").setTextAlign("center"))
          .addStyle("level", new ReportStyle("level").setBkgColor("EEEEEE").setTextColor("333333").setBorderBottom("333333", "solid", 3).setNoWrap(true).setTextWeight("bold").setTextSize(12))
          .addStyle("normal-left", new ReportStyle("normal-left").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("left").setBorderBottom("333333", "dotted", 1))
          .addStyle("normal-center", new ReportStyle("normal-center").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("center").setBorderBottom("333333", "dotted", 1))
          .addStyle("normal-right", new ReportStyle("normal-right").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("right").setBorderBottom("333333", "dotted", 1))
          .addStyle("normal-date", new ReportStyle("normal-date").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("center").setFormat("date").setBorderBottom("333333", "dotted", 1))
          .addRow(new ReportRow().addCell(new ReportCell("ID", "header-center")).addCell(new ReportCell("Name", "header")).addCell(new ReportCell("Company", "header")).addCell(new ReportCell("Visits", "header-center")).addCell(new ReportCell("Registered", "header-center")).addCell(new ReportCell("Orders", "header-center"))
  );
  def colWidths = [2f,10f,10f,2f,4f,3f]
  rt.setColWidths(colWidths)

  Date dateIni = SomeUtils.strToDate(getParameter(action, "registered_ini"), action.getDefaultLanguage());
  Date dateEnd = SomeUtils.strToDate(getParameter(action, "registered_end"), action.getDefaultLanguage());
  Long level = SomeUtils.strToLong(getParameter(action, "level"));
  String access = getParameter(action, "active");
  boolean taxExempt = "Y".equalsIgnoreCase(getParameter(action, "taxExempt"));
  boolean feeExempt = "Y".equalsIgnoreCase(getParameter(action, "feeExempt"));
  Integer maxRows = SomeUtils.strToInteger(getParameter(action, "maxrows"));

  String query = "select {t_user.*}, t.numOrders from (select t_user.idUser, count(t_order.idOrder) as numOrders, count(taxExempt.preferenceValue) as numTax, count(feeExempt.preferenceValue) as numFee\n" +
          "from t_user left join t_order on t_user.idUser=t_order.user_idUser \n" +
          "left join store2.t_user_preference taxExempt on t_user.idUser=taxExempt.user_idUser and taxExempt.preferenceCode='TAX_EXEMPTION'\n" +
          "left join store2.t_user_preference feeExempt on t_user.idUser=feeExempt.user_idUser and feeExempt.preferenceCode='FEE_EXEMPTION'\n" +
          "group by t_user.idUser) t left join t_user on t.idUser=t_user.idUser";

  String where = " where t_user.inventaryCode=:storeCode ";

  if (dateIni != null) {
    where += " and t_user.registerDate>=:dateIni ";
    rt.addFilter(action.getText("registered.after", "Registered after"), getParameter(action, "registered_ini"));
  }
  if (dateEnd != null) {
    where += " and t_user.registerDate<=:dateEnd ";
    rt.addFilter(action.getText("registered.before", "Registered before"), getParameter(action, "registered_end"));
  }
  if (level != null) {
    where += " and t_user.level_id=:level ";
    rt.addFilter(action.getText("customer.level", "Customer type"), getUserLevelName(level));
  }
  if ("active".equalsIgnoreCase(access)) {
    where += " and (t_user.blocked is null or t_user.blocked<>1) ";
    rt.addFilter(action.getText("only.active", "Only Active"), action.getText("yes", "YES"));
  } else if ("block".equalsIgnoreCase(access)) {
    where += " and t_user.blocked=1 ";
    rt.addFilter(action.getText("only.blocked", "Only Blocked"), action.getText("yes", "YES"));
  }
  if (taxExempt) {
    where += " and numTax>0 ";
    rt.addFilter(action.getText("tax.exempt", "Tax Exempt"), action.getText("yes", "YES"));
  }
  if (feeExempt) {
    where += " and numFee>0 ";
    rt.addFilter(action.getText("fee.exempt", "Fee Exempt"), action.getText("yes", "YES"));
  }

  def orderBy = " order by t_user.level_id, t_user.firstname"

  def sqlQuery = action.getDao().gethSession().createSQLQuery(query + where + orderBy).addEntity("t_user", User.class).addScalar("numOrders");
  sqlQuery.setString("storeCode", action.getStoreCode());
  if (dateIni != null) sqlQuery.setDate("dateIni", SomeUtils.dateIni(dateIni));
  if (dateEnd != null) sqlQuery.setDate("dateEnd", SomeUtils.dateEnd(dateEnd));
  if (level != null) sqlQuery.setLong("level", level);

  if (maxRows != null) sqlQuery.setMaxResults(maxRows);

  List<Object[]> list = sqlQuery.list();
  def oldLevel = -1
  for (Object[] o: list) {
    def u = (User) o[0];
    def levelId = (u.level!=null) ? u.level.id : 0;
    if (level==null && oldLevel!=levelId) {
      rt.addRow(levelRow( (u.level!=null) ? u.level.getName(action.getDefaultLanguage()) : action.getText("without.level", "[without level]") ))
      oldLevel = levelId
    }
    rt.addRow(objectToRow(o))
  };
  return rt
}

if (input == "info") output = getInfo()
else if (input == "config") output = getConfig()
else if (input == "execute") output = execute()
else output = ""
