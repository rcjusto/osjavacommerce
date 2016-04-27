import org.store.core.beans.Product
import org.store.core.beans.ProductLang
import org.store.core.utils.reports.ReportFilterField

def getParameter(a, p) {
  def paramValue = a.getRequest().getParameter(p);
  return (paramValue != null && !"".equals(paramValue)) ? paramValue : "";
}

def getInfo() {
  return [name: action.getText("procesing.products.techdata.htmlimport", "Importar HTML de Techdata España"), description: action.getText("procesing.products.techdata.htmlimport.description", "Importa la informacion que aparece en la pagina web del sitio Techdata España, en el campo Especificaciones del producto")]
}

def getConfig() {
  StringBuffer buff = new StringBuffer()
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "append", getParameter(action, "append"), action.getText("admin.features.fill.mode", "Features fill mode")).setCssClass("field string-medium").addOption("Y", action.getText("admin.append", "Append")).addOption("N", action.getText("admin.replace", "Replace")).toString());
  return buff.toString()
}

def execute() {
  def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
  def slurper = new XmlSlurper(tagsoupParser)
  def dao = action.getDao()
  def appendTo = getParameter(action, "append")
  def product = dao.get(Product.class, idProduct)

  if (product && product.getMfgPartnumber()) {
    def url = "http://prf.icecat.biz/index.cgi?product_id=${product.getMfgPartnumber()};mi=start;smi=product;shopname=techdata-es;lang=sp"
    def reader = new InputStreamReader(new URI(url).toURL().openStream(), "UTF-8")
    def htmlParser = slurper.parse(reader)
  //  def htmlParser = slurper.parse(new File("/media/trabajo/proyectos/stores/version2/doc/parsers/${product.getMfgPartnumber()}.html"))


// product name
    productName = ""
    htmlParser.'**'.findAll {it.@class == 'titlegev'}.each {
      productName = "${it}";
    }

// descripcion
    def formBuy = htmlParser.'**'.find {it.@name == 'buy'};
    if (formBuy) {
      def rowDesc = formBuy.table[0].tr[1]
      if (rowDesc) {
        println(rowDesc)
      }
    }

// attributes
    def spec = htmlParser.'**'.find {it.@class == 'specs'};
    def features  = '<table class="specs">'
    spec.'**'.findAll { it.@class == 'ds_label' || it.@class == 'ds_header'}.each {
      if (it.@class == 'ds_header') features += '<tr><th colspan="2" class="section">' + it.text() + '</th></tr>'
      else {
        def value = it.parent().children().findAll {it.@class == 'ds_data'}
        def valueText = ''
        if (value.text() == '') {
          def src = (it.parent().td[1].img[0].@src).text()
          if (src != '') {
            valueText = (src.endsWith('yes.gif')) ? 'Si' : 'No'
          }
        } else {
          valueText = value.text()
        }

        features += '<tr><th>' + it.text() + '</th><td>' + valueText + '</td></tr>'

        /*
        def attribute = dao.getAttributeByName(lang, it.text(), title.text(), true)
        def property = dao.getProductProperty(product, attribute)
        if (!property) {
          property = new ProductProperty()
          property.setProduct(product)
          property.setAttribute(attribute)
        }
        property.setPropertyValue(valueText)
        dao.save(property)
        */

      }
    }
    features += '</table>'

    for(String language : action.getLanguages()) {
      def pLang = dao.getProductLang(product, language);
      if (!pLang) {
        pLang = new ProductLang()
        pLang.setProduct(product)
        pLang.setProductLang(language)
      }
      pLang.setFeatures( ((appendTo=='Y' && pLang.getFeatures()) ? pLang.getFeatures() : '') +   features)
      dao.save(pLang)
    }


  } else return "Product with id "+idProduct.toString()+", not found or has empty manufacter code"
  return ""
}


if (input == "info") output = getInfo()
else if (input == "config") output = getConfig()
else if (input == "execute") output = execute()
else output = ""

