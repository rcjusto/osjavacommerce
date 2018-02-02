import org.store.core.beans.Product
import org.store.core.beans.ProductLang
import org.store.core.beans.ProductProperty
import org.store.core.beans.Resource
import org.store.core.utils.reports.ReportFilterField
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils

def getParameter(a, p) {
  def paramValue = a.getRequest().getParameter(p);
  return (paramValue != null && !"".equals(paramValue)) ? paramValue : "";
}

def getInfo() {
  return [name: action.getText("procesing.products.icecat.live", "Importar datos de Icecat"), description: action.getText("procesing.products.icecat.live.description", "Importa la informacion sobre el producto que aparece en el catalogo de Icecat")]
}

def getConfig() {
  StringBuffer buff = new StringBuffer()
  buff.append(new ReportFilterField(ReportFilterField.TYPE_TEXT, "icecatUsername", getParameter(action, "icecatUsername"), action.getText("icecat.username", "Icecat UserName")).setCssClass("field string-medium cookie-saved").toString());
  buff.append(new ReportFilterField(ReportFilterField.TYPE_TEXT, "icecatPassword", getParameter(action, "icecatPassword"), action.getText("icecat.password", "Icecat Password")).setCssClass("field string-medium cookie-saved").toString());
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "productName", getParameter(action, "productName"), action.getText("icecat.product.name.behaviour", "Icecat Product Name")).setCssClass("field string-medium").addOption("append", action.getText("import.append", "Append")).addOption("replace", action.getText("import.replace", "Replace")).addOption("", action.getText("import.nothing", "Do not import")).toString());
  buff.append(getTextField('shortDescription','icecat.short.description.behaviour','Icecat Short Description').toString());
  buff.append(getTextField('longDescription','icecat.long.description.behaviour','Icecat Long Description').toString());
  buff.append(getTextField('shortSummary','icecat.short.summary.behaviour','Icecat Short Summary').toString());
  buff.append(getTextField('longSummary','icecat.long.summary.behaviour','Icecat Long Summary').toString());
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "features", getParameter(action, "features"), action.getText("icecat.features.behaviour", "Icecat Features")).setCssClass("field string-medium").addOption("delete", action.getText("import.delete.import", "Delete olds and Import")).addOption("replace", action.getText("import.replace", "Replace")).addOption("", action.getText("import.nothing", "Do not import")).toString());
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "mainImage", getParameter(action, "mainImage"), action.getText("icecat.mail.image.behaviour", "Icecat Main Image")).setCssClass("field string-medium").addOption("download", action.getText("import.download", "Only download")).addOption("process", action.getText("import.process", "Process")).addOption("", action.getText("import.nothing", "Do not import")).toString());
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "galleryImages", getParameter(action, "galleryImages"), action.getText("icecat.gallery.images.behaviour", "Icecat Gallery Images")).setCssClass("field string-medium").addOption("download", action.getText("import.download", "Only download")).addOption("process", action.getText("import.process", "Process")).addOption("", action.getText("import.nothing", "Do not import")).toString());
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "manual", getParameter(action, "manual"), action.getText("icecat.product.manual.behaviour", "Icecat Product Manual Images")).setCssClass("field string-medium").addOption("append", action.getText("import.append", "Append")).addOption("", action.getText("import.nothing", "Do not import")).toString());
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "pdf", getParameter(action, "pdf"), action.getText("icecat.product.pdf.behaviour", "Icecat Product Pdf Images")).setCssClass("field string-medium").addOption("append", action.getText("import.append", "Append")).addOption("", action.getText("import.nothing", "Do not import")).toString());
  buff.append(new ReportFilterField(ReportFilterField.TYPE_SELECT, "manufacturerUrl", getParameter(action, "manufacturerUrl"), action.getText("icecat.manufacturer.url.behaviour", "Icecat Manufacturer Url")).setCssClass("field string-medium").addOption("replace", action.getText("import.replace", "Replace")).addOption("", action.getText("import.nothing", "Do not import")).toString());
  return buff.toString()
}

def getTextField(name, label, title) {
  return new ReportFilterField(ReportFilterField.TYPE_SELECT, name, getParameter(action, name), action.getText(label, title))
          .setCssClass("field string-medium")
          .addOption("append.description", action.getText("import.append.description", "Append to Description"))
          .addOption("append.features", action.getText("import.append.features", "Append to Features"))
          .addOption("append.information", action.getText("import.append.information", "Append to Additional Information"))
          .addOption("replace.description", action.getText("import.replace.description", "Replace Description"))
          .addOption("replace.features", action.getText("import.replace.features", "Replace Features"))
          .addOption("replace.information", action.getText("import.replace.information", "Replace Additional Information"))
          .addOption("", action.getText("import.nothing", "Do not import"))

}

def execute() {
  def res = ""
  for(String language : action.getLanguages()) {
    res += executeForLanguage(language, language == action.getDefaultLanguage())
  }
  return res;
}

def executeForLanguage(language, fullData) {
  def slurper = new XmlSlurper()
  def dao = action.getDao()
  def userName = getParameter(action, "icecatUsername")
  def password = getParameter(action, "icecatPassword")
  def productNameParam = getParameter(action, "productName")
  def shortDescriptionParam = getParameter(action, "shortDescription")
  def longDescriptionParam = getParameter(action, "longDescription")
  def shortSummaryParam = getParameter(action, "shortSummary")
  def longSummaryParam = getParameter(action, "longSummary")
  def featuresParam = getParameter(action, "features")
  def mainImageParam = getParameter(action, "mainImage")
  def galleryImagesParam = getParameter(action, "galleryImages")
  def manualParam = getParameter(action, "manual")
  def pdfParam = getParameter(action, "pdf")
  def manufacturerUrlParam = getParameter(action, "manufacturerUrl")

  def product = dao.get(Product.class, idProduct)

  if (product && product.getPartNumber()) {
    def url = "http://data.icecat.biz/xml_s3/xml_server3.cgi?prod_id=${product.getPartNumber()};vendor=${product.getManufacturer().getManufacturerName()};lang=${language};output=productxml"
    def conn = url.toURL().openConnection()

    if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
      def authString = (userName + ":" + password).getBytes().encodeBase64().toString()
      conn.setRequestProperty( "Authorization", "Basic ${authString}" )
    }

    if( conn.responseCode == 200 ) {
      def xml = slurper.parseText( conn.content.text )

     // def xml = slurper.parse(new File("/media/trabajo/proyectos/stores/version2/web/test/${product.getManufacturer().getManufacturerName()}_${product.getPartNumber()}.xml"))

      if (!xml) {
        return "Error connecting icecat";
      }

      def error = xml.Product.@ErrorMessage
      if (StringUtils.isNotEmpty("${error}")) return "ERROR in ${product.getManufacturer().getManufacturerName()}, ${product.getPartNumber()} => ${error}"

// product data
      def icecatId = xml.Product.@Prod_id
      def imageZ = xml.Product.@HighPic
      def imageN = xml.Product.@LowPic
      def imageS = xml.Product.@ThumbPic
      def name = xml.Product.@Title
      def category = xml.Product.Category.Name.@Value
      def shortDesc = xml.Product.ProductDescription.@ShortDesc
      def longDesc = xml.Product.ProductDescription.@LongDesc
      def manualUrl = xml.Product.ProductDescription.@ManualPDFURL
      def pdfUrl = xml.Product.ProductDescription.@PDFURL
      def manufacturerUrl = xml.Product.ProductDescription.@URL
      def warranty = xml.Product.ProductDescription.@WarrantyInfo
      def shortSummary = xml.Product.SummaryDescription.ShortSummaryDescription
      def longSummary = xml.Product.SummaryDescription.LongSummaryDescription
      def manufacturer = xml.Product.Supplier.@Supplier


// attributes
      if (fullData) { // esta parte se ejecuta una sola vez para actualizar datos que no dependen del idioma

        if (featuresParam=='delete')  { // eliminar attributos viejos
          dao.deleteProductProperties(product);
        }
        if (featuresParam!='') { // importar attributos
          xml.Product.ProductFeature.each {
            def featureCategory = getCategoryFeature(xml, it.@CategoryFeatureGroup_ID)
            def attribute = dao.getAttributeByName(language, "${it.Feature.Name.@Value}", "${featureCategory}", true)
            def property = dao.getProductProperty(product, attribute)
            if (!property) {
              property = new ProductProperty()
              property.setProduct(product)
              property.setAttribute(attribute)
            }
            def value = it.@Presentation_Value
            if ("${value}".trim()=='Y') {
              if (language=='es') value = 'Si'
              else value = 'Yes'
            }
            if ("${value}".trim()=='N') {
              value = 'No'
            }
            property.setPropertyValue("${value}")
            dao.save(property)
          }
        }

        // imagenes
        if (StringUtils.isNotEmpty("${imageZ}")) {
          if (mainImageParam=='download') downloadImage(imageZ, product)
          else if (mainImageParam=='process') saveProductImage(imageZ, product)
        }
        xml.Product.ProductGallery.ProductPicture.each {
          if (galleryImagesParam=='download') downloadImage(it.@Pic, product)
          else if (galleryImagesParam=='process') saveProductImage(it.@Pic, product)
        }

        // pdfs
        if (StringUtils.isNotEmpty("${manualUrl}") && manualParam=='append') saveResource(manualUrl, product, action.getText('product.manual.name','Manual'))
        if (StringUtils.isNotEmpty("${pdfUrl}") && pdfParam=='append') saveResource(pdfUrl, product, action.getText('product.pdf.name','Pdf'))

        // url del producto en el sitio del manufacturer
        if (manufacturerUrl && manufacturerUrlParam=='replace') product.setUrlManufacturer("${manufacturerUrl}")
        dao.save(product)
      }

      def pLang = dao.getProductLang(product, language);
      if (!pLang) {
          pLang = new ProductLang()
          pLang.setProduct(product)
          pLang.setProductLang(language)
      }
      if (StringUtils.isNotEmpty("${name}")) {
        if (productNameParam=='replace') pLang.setProductName("${name}")
        else if (productNameParam=='append') pLang.setProductName("${pLang.getProductName()} ${name}")
      }

      if (StringUtils.isNotEmpty("${shortDesc}") && StringUtils.isNotEmpty("${shortDescriptionParam}")) processText(shortDesc, shortDescriptionParam, pLang)
      if (StringUtils.isNotEmpty("${longDesc}") && StringUtils.isNotEmpty("${longDescriptionParam}")) processText(longDesc, longDescriptionParam, pLang)
      if (StringUtils.isNotEmpty("${shortSummary}") && StringUtils.isNotEmpty("${shortSummaryParam}")) processText(shortSummary, shortSummaryParam, pLang)
      if (StringUtils.isNotEmpty("${longSummary}") && StringUtils.isNotEmpty("${longSummaryParam}")) processText(longSummary, longSummaryParam, pLang)
      dao.save(pLang)
    } else return "Error connecting icecat";
  } else return "Product with id "+idProduct.toString()+", not found or has empty manufacter code"
  return ""
}

def processText(value, method, pLang) {
  value = "${value}".replaceAll("\\\\n","<br/>")
  if (method=='append.description') pLang.setDescription("${pLang.getDescription()} ${value}")
  else if (method=='append.features') pLang.setFeatures("${pLang.getFeatures()} ${value}")
  else if (method=='append.information') pLang.setInformation("${pLang.getInformation()} ${value}")
  else if (method=='replace.description') pLang.setDescription("${value}")
  else if (method=='replace.features') pLang.setFeatures("${value}")
  else if (method=='replace.information') pLang.setInformation("${value}")
}

def getCategoryFeature(xml, id) {
  def node = xml.Product.CategoryFeatureGroup.find {it.@ID == id}
  return (node) ? node.FeatureGroup.Name.@Value : null
}

def saveResource(url, product, desc) {
  // get extension
  def mid = "${url}".lastIndexOf("/");
  def fileName = "${url}".substring(mid+1,"${url}".length());
  mid = "${url}".lastIndexOf(".");
  def ext = (mid) ? "${url}".substring(mid+1,"${url}".length()) : "pdf";

  // create resource
  def res = new Resource()
  res.setFileName(fileName)
  res.setInventaryCode(product.getInventaryCode())
  res.setResourceType("download");
  res.setResourceName(desc)
  action.getDao().save(res)

  // download file
  def path = action.getServletContext().getRealPath("/stores/" + action.getStoreCode() + "/resources")
  def file = new File("${path}/${res.id}.${ext}")
  def fileOS = file.newOutputStream()
  fileOS << new URL("${url}").openStream()
  fileOS.close()

  // assign to product
  def resources = product.getProductResources()
  if (resources==null) {
    resources = new HashSet();
    product.setProductResources(resources);
  }
  resources.add(res)

}

def saveProductImage(url, product) {
  // get extension
  def mid = "${url}".lastIndexOf(".");
  def ext = (mid) ? "${url}".substring(mid+1,"${url}".length()) : "jpg";

  // download file
  def file = File.createTempFile("gr_image","."+ext)
  def fileOS = file.newOutputStream()
  fileOS << new URL("${url}").openStream()
  fileOS.close()

  //process file
  if (file.exists()) action.getImageResolver().processImage(product, file, ext)

  //delete file
  if (file.exists()) file.delete();
}

def downloadImage(url, product) {
  // get extension
  def mid = "${url}".lastIndexOf(".");
  def ext = (mid) ? "${url}".substring(mid+1,"${url}".length()) : "jpg";

  // download file
  def path = action.getServletContext().getRealPath("/stores/" + action.getStoreCode() + "/uploads")
  FileUtils.forceMkdir(new File("${path}"))
  def file = new File("${path}/${product.partNumber}.${ext}")
  def fIndex = 1;
  while(file.exists()) {
    file = new File("${path}/${product.partNumber}_${fIndex}.${ext}")
    fIndex++;
  }

  def fileOS = file.newOutputStream()
  fileOS << new URL("${url}").openStream()
  fileOS.close()
}

if (input == "info") output = getInfo()
else if (input == "config") output = getConfig()
else if (input == "execute") output = execute()
else output = ""

