package vampire

def getInfo() {
  return [name: "Price Grabber", url: "http://www.pricegrabber.com"]
}

def list() {
    def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
    def slurper = new XmlSlurper(tagsoupParser)
    def htmlParser = slurper.parse(url)
    def result = []

    htmlParser.'**'.findAll{'product_item' in ((it.@class).toString().split(' '))}.each {
        it.'**'.findAll{ it.@class == 'item_details' }.each {
            def code = it.div[0].text().trim().split(' ')[0]
            def name = it.h3.a.text()
            def url = it.h3.a.@href.toString()
            result += ['code': code, 'name': name, 'url': url]
        }
    }

    return result;
}

def process() {
    def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
    def slurper = new XmlSlurper(tagsoupParser)
    def htmlParser = slurper.parse(url)
    def result = ['type':'product']

    def pi = htmlParser.'**'.find{it.@id=='product_info'}

    def p = pi.'**'.find{it.name()=='p'}
    def c = (p.text() - p.a.text()).toString().trim().split('MPN:')
    result['code'] = c[1].trim()

    result['name'] = nodeText(pi.'**'.find{it.name()=='h1'})
    image = pi.'**'.find{it.@class=='product_img'}.img.@src.toString()
    if (image.endsWith('_125.jpg')) image = image.replace('_125.jpg','_640.jpg')
    result['image'] = image;
    result['price'] = getPrice(pi.'**'.find{it.@class=='price'})
    result['desc'] = nodeText(htmlParser.'**'.find{it.@id=='product_details_description'})

    def pds = htmlParser.'**'.find{it.@id=='prod_details_summary'}
    if (pds!=null) {
        pds.'**'.findAll{it.name()=='p'}.each {
            if (nodeText(it.span)=='Manufacturer:') result['manufacturer'] = it.text() - it.span.text()
            if (nodeText(it.span)=='Lowest Price:') result['price'] = (it.text() - it.span.text()) - '$'
        }
    }

    def spec = new LinkedHashMap()
    def subSpecTitle = ''
    def subSpecArray = new LinkedHashMap()

    htmlParser.'**'.find{it.@id=='prod_detail_specs'}.'**'.findAll{it.name()=='p'}.each {
        if (it.@class=='subtitle') {
            if (!subSpecArray.isEmpty()) {
                if (subSpecTitle=='') subSpecTitle = '-'
                spec.put(subSpecTitle,subSpecArray)
            }
            subSpecTitle = nodeText(it)
            subSpecArray = new LinkedHashMap()
        } else {
            sn = nodeText(it.span[0])
            if (sn!='URL:') {
                subSpecArray.put(sn - ':', nodeText(it)-sn)
            }
        }
    }
    if (!subSpecArray.isEmpty()) {
        if (subSpecTitle=='') subSpecTitle = '-'
        spec.put(subSpecTitle,subSpecArray)
    }
    if (!spec.isEmpty()) result['spec'] = spec

    return result;
}

def nodeText(node) {
    return (node) ? node.text().trim() : ''
}

def getPrice(node) {
    s = (node) ? node.text().trim() : ''
    a = s.split('-')
    p = (a.length>1) ? a[1].trim() : a[0].trim()
    return p - '$'
}

if (doAction == "info") output = getInfo()
else if (doAction == "list") output = list()
else if (doAction == "process") output = process()
else output = ""

