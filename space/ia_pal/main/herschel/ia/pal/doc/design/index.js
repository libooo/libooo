/* -*-java-*- script */

/**
 * "Table of Contents" for package design
 */
{
    // header: required
    TOC.start();

    // body: here goes your information:
    TOC.title("Design Document", "html/pal-design.html");

    // add sections
    TOC.add(new DocLocal("Introduction", "html/ar01s02.html"));
    TOC.add(new DocLocal("Design principles", "html/ar01s03.html"));
    TOC.add(new DocLocal("ProductStorage and ProductPools", "html/ar01s04.html"));
    TOC.add(new DocLocal("Contexts", "html/ar01s05.html"));
    TOC.add(new DocLocal("Storage and PoolManagers", "html/ar01s06.html"));
    TOC.add(new DocLocal("Versioning, tagging and descriptors", "html/ar01s07.html"));
    TOC.add(new DocLocal("Queries", "html/ar01s08.html"));
    TOC.add(new DocLocal("Locking mechanism", "html/ar01s09.html"));
    TOC.add(new DocLocal("The PAL Common Test Suite", "html/ar01s10.html"));

    // footer: required
    TOC.end();
}
