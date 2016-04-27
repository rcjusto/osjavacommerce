import org.store.core.beans.Category
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

def categoryRow(Object[] o) {
  return new ReportRow()
  .addCell(new ReportCell(o[4], "category", 4))
}

def objectToRow(Object[] o) {
  return new ReportRow()
  .addCell(new ReportCell(o[0], "normal-left"))
  .addCell(new ReportCell(o[1], "normal-left"))
  .addCell(new ReportCell(o[2], "normal-center"))
  .addCell(new ReportCell(o[3], "normal-center"))
}

def getInfo() {
    return [name: action.getText("report.inventory.name", "Inventario"), description: action.getText("report.inventory.description", "Listado de productos")]
}

def getConfig() {
  StringBuffer buff = new StringBuffer()
  // combo de categorias
  buff.append("<p class=\"clearfix\"><label><span class=\"label\">" + action.getText("report.inventory.category", "Category") + "</span>")
  buff.append("<select name=\"category\" class=\"field string-medium tree\"><option value=\"\">"+action.getText("all.categories", "All categories")+"</option>" + action.dao.getCategoryOptionsForSelect(true, "es") + "</select></label></p>")

  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "archived", getParameter(action, "archived"), action.getText("report.inventory.archived", "Archived")).setCssClass("field string-short").addOption("", action.getText("all.products", "All products")).addOption("1", action.getText("archived", "Archived")).addOption("0", action.getText("no.archived", "Not archived")).toString());

  // como de activos o bloqueados
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "active", getParameter(action, "active"), action.getText("report.inventory.active", "Active")).setCssClass("field string-short").addOption("", action.getText("all.products", "All products")).addOption("1", action.getText("only.active", "Only actives")).addOption("0", action.getText("only.inactive", "Only Inactive")).toString());
  // stock minimo
  buff.append(new ReportFilterField(ReportFilterField.TYPE_TEXT, "stockUnder", getParameter(action, "stockUnder"), action.getText("report.inventory.stockUnder", "Stock Under")).setCssClass("field integer").toString());
  return buff.toString()
}

def execute() {
  def rt = new ReportTable(action.getText("report.inventary.name", "Inventario"))
          .setPageSize(ReportTable.PAGE_LETTER_H)
          .addStyle("header", new ReportStyle("header").setBkgColor("333333").setTextColor("FFFFFF").setNoWrap(true))
          .addStyle("header-center", new ReportStyle("header").setBkgColor("333333").setTextColor("FFFFFF").setNoWrap(true).setTextAlign("center"))
          .addStyle("category", new ReportStyle("category").setBkgColor("EEEEEE").setTextColor("333333").setBorderBottom("333333", "solid", 3).setNoWrap(true).setTextWeight("bold").setTextSize(12))
          .addStyle("normal-left", new ReportStyle("normal-left").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("left").setBorderBottom("333333", "solid", 1))
          .addStyle("normal-center", new ReportStyle("normal-center").setBkgColor("FFFFFF").setTextColor("333333").setTextAlign("center").setBorderBottom("333333", "solid", 1).setNoWrap(true))
          .addRow(new ReportRow()
            .addCell(new ReportCell("PartNumber", "header"))
            .addCell(new ReportCell("Name", "header"))
            .addCell(new ReportCell("Stock", "header-center"))
            .addCell(new ReportCell("Sales", "header-center"))
  )

  def colWidths = [6f,18f,2f,2f]
  rt.setColWidths(colWidths)

  Category category = action.dao.getCategory(SomeUtils.strToLong(getParameter(action, "category")));
  Long[] categoryIds = (category!=null) ? action.dao.getIdCategoryList(category, false) : null

  Integer archived = SomeUtils.strToInteger(getParameter(action, "archived"));
  Integer active = SomeUtils.strToInteger(getParameter(action, "active"));
  Integer stockUnder = SomeUtils.strToInteger(getParameter(action, "stockUnder"));
  Integer maxRows = SomeUtils.strToInteger(getParameter(action, "maxrows"));

  String query = "select distinct t_product.partNumber, t_product_lang.productName, t_product.stock, t_product.sales, t_category_lang.categoryName, t_product.category_idCategory from t_product left join t_product_t_category on t_product.idProduct=t_product_t_category.t_product_idProduct left join t_product_lang on t_product.idProduct=t_product_lang.product_idProduct and t_product_lang.productLang='"+action.getDefaultLanguage()+"' left join t_category_lang on t_product.category_idCategory=t_category_lang.category_idCategory and t_category_lang.categoryLang='"+action.getDefaultLanguage()+"'";

  String where = " where t_product.inventaryCode=:storeCode ";

  if (categoryIds != null && categoryIds.length>0) {
    String idsStr = categoryIds.each {it.toString()}.join(",")
    where += " and t_product_t_category.productCategories_idCategory in ("+idsStr+") ";
    rt.addFilter(action.getText("report.inventory.category", "Category"), category.getCategoryName(action.getDefaultLanguage()));
  }
  if (archived != null) {
    where += (archived.equals(1)) ? " and t_product.archived=1 " : " and (t_product.archived is null or t_product.archived=0) ";
    rt.addFilter(action.getText("report.inventory.archived", "Archived"), (archived.equals(1)) ? action.getText("archived", "Archived") : action.getText("no.archived", "Not archived") );
  }
  if (active != null) {
    where += (active.equals(1)) ? " and t_product.active=1 " : " and (t_product.active is null or t_product.active=0) ";
    rt.addFilter(action.getText("report.inventory.active", "Active"), (active.equals(1)) ? action.getText("only.active", "Only actives") : action.getText("only.inactive", "Only Inactive") );
  }
  if (stockUnder!=null) {
    where += " and t_product.stock<" + stockUnder.toString() + " ";
    rt.addFilter(action.getText("report.inventory.stockUnder", "Stock Under"), stockUnder.toString());
  }

  def orderBy = " order by t_product.category_idCategory";

  def sqlQuery = action.getDao().gethSession().createSQLQuery(query + where + orderBy);
  sqlQuery.setString("storeCode", action.getStoreCode());

  if (maxRows != null) sqlQuery.setMaxResults(maxRows);

  List<Object[]> list = sqlQuery.list();
  def oldCategory = 0;
  for (Object[] o: list) {
    if (oldCategory!=o[5]) {
      rt.addRow(categoryRow(o))
      oldCategory = o[5]
    }
    rt.addRow(objectToRow(o));
  }
  return rt
}

if (input == "info") output = getInfo()
else if (input == "config") output = getConfig()
else if (input == "execute") output = execute()
else output = ""
