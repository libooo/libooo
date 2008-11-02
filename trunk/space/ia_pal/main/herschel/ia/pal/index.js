{
    // Always start with...
    TOC.start();
    
    // Title for root chapter...
    TOC.title("Product Access Layer Framework","doc/index.html");
    
    // Access to the CHANGELOG...
    TOC.add(new DocLocal("CHANGELOG","CHANGELOG"));
    
    // Developer web pages: escapes the frameset...
    TOC.add(new DocLink("DP Developer Web Pages","http://www.rssd.esa.int/SD-general/Projects/Herschel/hscdt/docsDpDeveloper.shtml"));

    // Relative link DP doc: escapes the frameset...
    TOC.add(new DocLink("DP Documentation","../doc/index.html",true));
    
    // A separator...
    TOC.add(new DocSep());
    
    // now the PAL specific documentation:

    // Developer Guide
    TOC.include("doc/guide");

    // Design Document
    TOC.include("doc/design");

    // Javadoc
    TOC.add(new DocLocal("Java API", "../../api/herschel/ia/pal/package-summary.html"));
    
    // Always end with...
    TOC.end();
}
