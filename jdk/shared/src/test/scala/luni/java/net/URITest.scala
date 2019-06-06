package luni.java.net

import java.net.{MalformedURLException, URI, URISyntaxException}

import org.scalatest.FunSuite

class URITest extends FunSuite {

  private val isScalaJS = System.getProperty("java.vm.name") == "Scala.js"

  private var uris: Array[URI] = null

  @throws[URISyntaxException]
  private def getUris: Array[URI] = {
    if (uris != null) return uris
    uris = Array[URI](
      // single arg constructor
      new URI("http://user%60%20info@host/a%20path?qu%60%20ery#fr%5E%20ag"),
      // escaped octets for illegal chars
      new URI(
        "http://user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g"
      ),
      // escaped octets for unicode chars
      new URI(
        "ascheme://user\u00DF\u00A3info@host:0/a\u20ACpath?qu\u00A9\u00AEery#fr\u00E4\u00E8g"
      ),
      // unicode chars equivalent to = new
      // URI("ascheme://user\u00df\u00a3info@host:0/a\u0080path?qu\u00a9\u00aeery#fr\u00e4\u00e8g"),
      // multiple arg constructors
// FIXME:
//      new URI(
//        "http",
//        "user%60%20info",
//        "host",
//        80,
//        "/a%20path",
//        "qu%60%20ery",
//        "fr%5E%20ag"
//      ),
//      // escaped octets for illegal
//      new URI(
//        "http",
//        "user%C3%9F%C2%A3info",
//        "host",
//        -1,
//        "/a%E2%82%ACpath",
//        "qu%C2%A9%C2%AEery",
//        "fr%C3%A4%C3%A8g"
//      ),
      // escaped octets for unicode
      new URI(
        "ascheme",
        "user\u00DF\u00A3info",
        "host",
        80,
        "/a\u20ACpath",
        "qu\u00A9\u00AEery",
        "fr\u00E4\u00E8g"
      ),
      // URI("ascheme", "user\u00df\u00a3info", "host", 80,
      // "/a\u0080path", "qu\u00a9\u00aeery", "fr\u00e4\u00e8g"),
      new URI("http", "user` info", "host", 81, "/a path", "qu` ery", "fr^ ag"), // illegal chars
      new URI("http", "user%info", "host", 0, "/a%path", "que%ry", "f%rag"),     // % as illegal char, not escaped octet
      // urls with undefined components
      new URI("mailto", "user@domain.com", null),                 // no host, path, query or fragment
      new URI("../adirectory/file.html#"),                        // relative path with empty fragment;
      new URI("news", "comp.infosystems.www.servers.unix", null), //
      new URI(null, null, null, "fragment"),                      // only fragment
      new URI("telnet://server.org"),                             // only host
      new URI("http://reg:istry?query"),                          // malformed hostname, therefore registry-based,
      // with query
      new URI("file:///c:/temp/calculate.pl?")
    )
    uris
  }

  test("ConstructorLjava_lang_String") {
    val constructorTests = Array[String](
      "http://user@www.google.com:45/search?q=helpinfo#somefragment",         // http with authority, query and fragment
      "ftp://ftp.is.co.za/rfc/rfc1808.txt",                                   // ftp
      "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles", // gopher
      "mailto:mduerst@ifi.unizh.ch",                                          // mailto
      "news:comp.infosystems.www.servers.unix",                               // news
      "telnet://melvyl.ucop.edu/",                                            // telnet
      "http://123.24.17.98/test",                                             // IPv4 authority
      "http://www.google.com:80/test",                                        // domain name authority
      "http://joe@[3ffe:2a00:100:7031::1]:80/test",                           // IPv6 authority, with userinfo and port
      "/relative",                                                            // relative starting with /
      "//relative",                                                           // relative starting with //
      "relative",                                                             // relative with no /
      "#fragment",                                                            // relative just with fragment
      "http://user@host:80",                                                  // UI, host,port
      "http://user@host",                                                     // ui, host
      "http://host",                                                          // host
      "http://host:80",                                                       // host,port
      "http://joe@:80",                                                       // ui, port (becomes registry-based)
      "file:///foo/bar",                                                      // empty authority, non empty path
      "ht?tp://hoe@host:80",                                                  // miscellaneous tests
      "mai/lto:hey?joe#man",
      "http://host/a%20path#frag",       // path with an escaped octet for space char
      "http://host/a%E2%82%ACpath#frag", // path with escaped octet for unicode char, not USASCII
      "http://host/a\u20ACpath#frag",    // path with unicode char, not USASCII equivalent to
      // = "http://host/a\u0080path#frag",
      "http://host%20name/", // escaped octets in host (becomes
      // registry based)
      "http://host\u00DFname/", // unicodechar in host (becomes
      // equivalent to = "http://host\u00dfname/",
      "ht123-+tp://www.google.com:80/test"
    )
    for (s <- constructorTests) {
      new URI(s)
    }
    val constructorTestsInvalid = Array[String](
      "http:///a path#frag", // space char in path, not in escaped
      // octet form, with no host
      "http://host/a[path#frag", // an illegal char, not in escaped
      // octet form, should throw an
      // exception
      "http://host/a%path#frag", // invalid escape sequence in path
      "http://host/a%#frag",     // incomplete escape sequence in path
      "http://host#a frag",      // space char in fragment, not in
      // escaped octet form, no path
      "http://host/a#fr#ag", // illegal char in fragment
      "http:///path#fr%ag",  // invalid escape sequence in fragment,
      // with no host
      "http://host/path#frag%", // incomplete escape sequence in
      // fragment
      "http://host/path?a query#frag", // space char in query, not
      // in escaped octet form
      "http://host?query%ag", // invalid escape sequence in query, no
      // path
      "http:///path?query%",       // incomplete escape sequence in query,
      "mailto:user^name@fklkf.com" // invalid char in scheme
    )
    val constructorTestsInvalidIndices = Array[Int](9, 13, 13, 13, 13, 16, 15, 21, 18, 17, 18, 11)
    (constructorTestsInvalid zip constructorTestsInvalidIndices).foreach {
      case (s, i) =>
        val ex = intercept[URISyntaxException] {
          new URI(s)
        }
        assert(
          isScalaJS || ex.getIndex == i,
          "Wrong index in URISytaxException for: " + s + " expected: " + i + ", received: " + ex.getIndex
        )
    }

    val invalid2 = Array(
      // authority validation
      "http://user@[3ffe:2x00:100:7031::1]:80/test",
      // IPv6 authority
      "http://[ipv6address]/apath#frag", // malformed
      "http://[ipv6address/apath#frag",  // malformed ipv6 address
      "http://ipv6address]/apath#frag",  // illegal char in host name
      "http://ipv6[address/apath#frag",
      "http://ipv6addr]ess/apath#frag",
      "http://ipv6address[]/apath#frag",
      // illegal char in username...
      "http://us[]er@host/path?query#frag",
      "http://host name/path",     // illegal char in authority
      "http://host^name#fragment", // illegal char in authority
      "telnet://us er@hostname/",  // missing components
// Disabled since it is trivial
//      "//",                        // Authority expected
//      "ascheme://",
      "ascheme:", // Scheme-specific part expected
      // scheme validation
      "a scheme://reg/",   // illegal char
      "1scheme://reg/",    // non alpha char as 1st char
      "asche\u00dfme:ssp", // unicode char , not USASCII
      "asc%20heme:ssp"     // escape octets
    )
    for (s <- invalid2) {
      withClue(s"For $s") {
        assertThrows[URISyntaxException] {
          new URI(s)
        }
      }
    }
  }

  test("Regression test for HARMONY-23") {
    val e = intercept[URISyntaxException] {
      new URI("%3")
    }
    assert(isScalaJS || 0 == e.getIndex)
  }

  test("Regression test for HARMONY-25") {
    // if port value is negative, the authority should be considered registry-based.
    var uri = new URI("http://host:-8096/path/index.html")
    assert(-1 == uri.getPort)
    assert(uri.getHost == null)
    assertThrows[URISyntaxException] {
      uri.parseServerAuthority
    }

    uri = new URI("http", "//myhost:-8096", null)
    assert(-1 == uri.getPort)
    assert(uri.getHost == null)
    assertThrows[URISyntaxException] {
      uri.parseServerAuthority
    }
  }

  test("URI_String") {
    val e = intercept[URISyntaxException] {
      new URI(":abc@mymail.com")
    }
    assert(isScalaJS || 0 == e.getIndex)

    val e1 = intercept[URISyntaxException] {
      new URI("path[one")
    }
    assert(isScalaJS || 4 == e1.getIndex)

    val e2 = intercept[URISyntaxException] {
      new URI(" ")
    }
    assert(isScalaJS || 0 == e2.getIndex)
  }

  test("ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_String") {
    val uri = new URI("mailto", "mduerst@ifi.unizh.ch", null)
    assert(uri.getUserInfo == null)
    assert(uri.getHost == null)
    assert(uri.getAuthority == null)
    assert(-1 == uri.getPort)
    assert(uri.getPath == null)
    assert(uri.getQuery == null)
    assert(uri.getFragment == null)
    assert("mduerst@ifi.unizh.ch" == uri.getSchemeSpecificPart)

    // scheme specific part can not be null
    assertThrows[URISyntaxException] {
      new URI("mailto", null, null)
    }
    // scheme needs to start with an alpha char
    assertThrows[URISyntaxException] {
      new URI("3scheme", "//authority/path", "fragment")
    }
    // scheme can not be empty string
    assertThrows[URISyntaxException] {
      new URI("", "//authority/path", "fragment")
    }
  }

  // check for URISyntaxException for invalid Server Authority
  test(
    "ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_StringILjava_lang_StringLjava_lang_StringLjava_lang_String"
  ) {
    construct1("http", "user", "host\u00DFname", -1, "/file", "query", "fragment") // unicode chars in host name

    // equivalent to construct1("http", "user", "host\u00dfname", -1,
    // "/file", "query", "fragment");
    construct1("http", "user", "host%20name", -1, "/file", "query", "fragment") // escaped octets in host name
    construct1("http", "user", "host name", -1, "/file", "query", "fragment")   // illegal char in host name
    construct1("http", "user", "host]name", -1, "/file", "query", "fragment")

    // missing host name
    construct1("http", "user", "", 80, "/file", "query", "fragment")
    construct1("http", "user", "", -1, "/file", "query", "fragment")

    // malformed ipv4 address
    construct1("telnet", null, "256.197.221.200", -1, null, null, null)
    construct1("ftp", null, "198.256.221.200", -1, null, null, null)

    // These tests fail on other implementations...
    // construct1("http", "user", null, 80, "/file", "query", "fragment");
    // //missing host name
    // construct1("http", "user", null, -1, "/file", "query", "fragment");
    // check for URISyntaxException for invalid scheme
    construct1("ht\u00DFtp", "user", "hostname", -1, "/file", "query", "fragment") // unicode chars in scheme

    // equivalent to construct1("ht\u00dftp", "user", "hostname", -1,
    // "/file",
    // "query", "fragment");
    construct1("ht%20tp", "user", "hostname", -1, "/file", "query", "fragment") // escaped octets in scheme
    construct1("ht tp", "user", "hostname", -1, "/file", "query", "fragment")   // illegal char in scheme
    construct1("ht]tp", "user", "hostname", -1, "/file", "query", "fragment")

    // relative path with scheme
    construct1("http", "user", "hostname", -1, "relative", "query", "fragment")

    // functional test
    val uri =
      new URI("http", "us:e@r", "hostname", 85, "/file/dir#/qu?e/", "qu?er#y", "frag#me?nt")
    assert("us:e@r" == uri.getUserInfo)
    assert("hostname" == uri.getHost)
    assert(85 == uri.getPort)
    assert("/file/dir#/qu?e/" == uri.getPath)
    assert("qu?er#y" == uri.getQuery)
    assert("frag#me?nt" == uri.getFragment)
    assert("//us:e@r@hostname:85/file/dir#/qu?e/?qu?er#y" == uri.getSchemeSpecificPart)
  }

  /*
   * helper method checking if the 7 arg constructor throws URISyntaxException
   * for a given set of parameters
   */
  private def construct1(
      scheme: String,
      userinfo: String,
      host: String,
      port: Int,
      path: String,
      query: String,
      fragment: String
  ): Unit = {
    withClue(
      s"schema:$scheme, userInfo:$userinfo, host:$host, port:$port, path:$path, query:$query, fragment:$fragment"
    ) {
      assertThrows[URISyntaxException] {
        new URI(scheme, userinfo, host, port, path, query, fragment)
      }
    }
  }

  test("ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String") {
    assertThrows[URISyntaxException] {
      new URI("http", "www.joe.com", "relative", "jimmy")
    }

    // valid parameters for this constructor
    new URI("http", "www.joe.com", "/path", "jimmy")

    // illegal char in path
    new URI("http", "www.host.com", "/path?q", "somefragment")

    // empty fragment
    new URI("ftp", "ftp.is.co.za", "/rfc/rfc1808.txt", "")

    // path with escaped octet for unicode char, not USASCII
    new URI("http", "host", "/a%E2%82%ACpath", "frag")

    // frag with unicode char, not USASCII
    // equivalent to = uri = new URI("http", "host", "/apath",
    // "\u0080frag");
    new URI("http", "host", "/apath", "\u20ACfrag")

    // Regression test for Harmony-1693
    new URI(null, null, null, null)

    // regression for Harmony-1346
    assertThrows[URISyntaxException] {
      new URI("http", ":2:3:4:5:6:7:8", "/apath", "\u20ACfrag")
    }
  }

  test(
    "ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String"
  ) {
    assertThrows[URISyntaxException] {
      new URI("http", "www.joe.com", "relative", "query", "jimmy")
    }
    // test if empty authority is parsed into undefined host, userinfo and port and
    // if unicode chars and escaped octets in components are preserved, illegal chars are quoted
    val uri = new URI("ht12-3+tp", "", "/p#a%E2%82%ACth", "q^u%25ery", "f/r\u00DFag")
    assert("ht12-3+tp" == uri.getScheme)
    assert(uri.getAuthority == null)
    assert(uri.getUserInfo == null)
    assert(uri.getHost == null)
    assert(-1 == uri.getPort)
// Should preserve if multiple args constructor
//    assert("/p#a%E2%82%ACth" == uri.getPath)
//    assert("q^u%25ery" == uri.getQuery)
    assert("f/r\u00DFag" == uri.getFragment)
//    assert("///p#a%E2%82%ACth?q^u%25ery" == uri.getSchemeSpecificPart)
//    assert("///p%23a%25E2%2582%25ACth?q%5Eu%2525ery" == uri.getRawSchemeSpecificPart)
//    assert("ht12-3+tp:///p%23a%25E2%2582%25ACth?q%5Eu%2525ery#f/r\u00dfag" == uri.toString)
//    assert("ht12-3+tp:///p%23a%25E2%2582%25ACth?q%5Eu%2525ery#f/r%C3%9Fag" == uri.toASCIIString)
  }

  test("fiveArgConstructor") {
    var uri = new URI("ftp", "[0001:1234::0001]", "/dir1/dir2", "query", "frag")
    assert("[0001:1234::0001]" == uri.getHost)

    // do not accept [] as part of invalid ipv6 address
    assertThrows[URISyntaxException] {
      new URI("ftp", "[www.abc.com]", "/dir1/dir2", "query", "frag")
    }
    // do not accept [] as part of user info
    assertThrows[URISyntaxException] {
      new URI("ftp", "[user]@host", "/dir1/dir2", "query", "frag")
    }
  }

  test("compareToLjava_lang_Object") {
    val compareToData = Array[(String, String, String)](
      // scheme tests
      ("http:test", "", ">"),            // scheme null, scheme not null
      ("", "http:test", "<"),            // reverse
      ("http:test", "ftp:test", ">"),    // schemes different
      ("/test", "/test", "="),           // schemes null
      ("http://joe", "http://joe", "="), // schemes same
      ("http://joe", "hTTp://joe", "="), // schemes same ignoring case

      // opacity : one opaque, the other not
      ("http:opaque", "http://nonopaque", ">"),
      ("http://nonopaque", "http:opaque", "<"),
      ("mailto:abc", "mailto:abc", "="), // same ssp
      ("mailto:abC", "mailto:Abc", ">"), // different, by case
      ("mailto:abc", "mailto:def", "<"), // different by letter
      ("mailto:abc#ABC", "mailto:abc#DEF", "<"),
      ("mailto:abc#ABC", "mailto:abc#ABC", "="),
      ("mailto:abc#DEF", "mailto:abc#ABC", ">"), // hierarchical tests..

      // different authorities
      ("//www.test.com/test", "//www.test2.com/test", "<"),
      ("/nullauth", "//nonnullauth/test", "<"), // one null authority
      ("//nonnull", "/null", ">"),
      ("/hello", "/hello", "="), // both authorities null

      // different userinfo
      ("http://joe@test.com:80", "http://test.com", ">"),
      ("http://jim@test.com", "http://james@test.com", ">"), // different hostnames
      ("http://test.com", "http://toast.com", "<"),
      ("http://test.com:80", "test.com:87", "<"),                  // different ports
      ("http://test.com", "http://test.com:80", "<"),              // different paths
      ("http://test.com:91/dir1", "http://test.com:91/dir2", "<"), // one null host
      ("http:/hostless", "http://hostfilled.com/hostless", "<"),   // queries
      ("http://test.com/dir?query", "http://test.com/dir?koory", ">"),
      ("/test?query", "/test", ">"),
      ("/test", "/test?query", "<"),
      ("/test", "/test", "="), // fragments
      ("ftp://test.com/path?query#frag", "ftp://test.com/path?query", ">"),
      ("ftp://test.com/path?query", "ftp://test.com/path?query#frag", "<"),
      ("#frag", "#frag", "="),
      ("p", "", ">"),
      ("http://www.google.com", "#test", ">") // miscellaneous
    )
    def ord(comp: Int): String = {
      if (comp == 0) "="
      else if (comp > 0) ">"
      else "<"
    }
    // test compareTo functionality
    compareToData.foreach {
      case (x, y, i) =>
        withClue(s"For $x and $y, expected $i") {
          val b = new URI(x)
          val r = new URI(y)
          assert(ord(b.compareTo(r)) == i)
        }
    }
  }

  test("compareTo2") {
    // test URIs with host names with different casing
    var uri  = new URI("http://AbC.cOm/root/news")
    var uri2 = new URI("http://aBc.CoM/root/news")
    assert(0 == uri.compareTo(uri2))
    assert(0 == uri.compareTo(uri2))
    // test URIs with one undefined component
    uri = new URI("http://abc.com:80/root/news")
    uri2 = new URI("http://abc.com/root/news")
    assert(uri.compareTo(uri2) > 0)
    assert(uri2.compareTo(uri) < 0)
    uri = new URI("http://user@abc.com/root/news")
    uri2 = new URI("http://abc.com/root/news")
    assert(uri.compareTo(uri2) > 0)
    assert(uri2.compareTo(uri) < 0)
  }

  test("createLjava_lang_String") {
    assertThrows[IllegalArgumentException] {
      URI.create("a scheme://reg/")
    }
  }

  test("equalsLjava_lang_Object") {
    val equalsData = Array[(String, String, Boolean)](
      ("", "", true), // null frags
      ("/path", "/path#frag", false),
      ("#frag", "#frag2", false),
      ("#frag", "#FRag", false),
      // case insensitive on hex escapes
      ("#fr%4F", "#fr%4f", true),
      ("scheme:test", "scheme2:test", false), // scheme stuff
      ("test", "http:test", false),
      ("http:test", "test", false),
      ("SCheme:test", "schEMe:test", true),
      // hierarchical/opaque mismatch
      ("mailto:jim", "mailto://jim", false),
      ("mailto://test", "mailto:test", false),
      // opaque
      ("mailto:name", "mailto:name", true),
      ("mailtO:john", "mailto:john", true),
      ("mailtO:john", "mailto:jim", false),
      // test hex case insensitivity on ssp
      ("mailto:te%4Fst", "mailto:te%4fst", true),
      ("mailto:john#frag", "mailto:john#frag2", false),
      // hierarchical
      ("/test", "/test", true), // paths
      ("/te%F4st", "/te%f4st", true),
      ("/TEst", "/teSt", false),
      ("", "/test", false), // registry based because they don't resolve properly to
      // server-based add more tests here
      ("//host.com:80err", "//host.com:80e", false),
      ("//host.com:81e%Abrr", "//host.com:81e%abrr", true),
      ("/test", "//auth.com/test", false),
      ("//test.com", "/test", false),
      ("//test.com", "//test.com", true), // hosts
      // case insensitivity for hosts
      ("//HoSt.coM/", "//hOsT.cOm/", true),
      ("//te%ae.com", "//te%aE.com", true),
      ("//test.com:80", "//test.com:81", false),
      ("//joe@test.com:80", "//test.com:80", false),
      ("//jo%3E@test.com:82", "//jo%3E@test.com:82", true),
      ("//test@test.com:85", "//test@test.com", false)
    )
    // test equals functionality
    equalsData.foreach {
      case (x, y, i) =>
        withClue(s"For $x and $y") {
          val b = new URI(x)
          val r = new URI(y)
          assert((b == r) == i)
        }
    }
  }

  test("equals2") {
    var uri  = new URI("http:///~/dictionary")
    var uri2 = new URI(uri.getScheme, uri.getAuthority, uri.getPath, uri.getQuery, uri.getFragment)
    assert(uri == uri)
    assert(uri2 == uri2)

    // test URIs with port number
    uri = new URI("http://abc.com%E2%82%AC:88/root/news")
    uri2 = new URI("http://abc.com%E2%82%AC/root/news")
    assert(uri != uri2)
    assert(uri2 != uri)

    // test URIs with host names with different casing
    uri = new URI("http://AbC.cOm/root/news")
    uri2 = new URI("http://aBc.CoM/root/news")
    assert(uri == uri2)
    assert(uri2 == uri)
  }

  test("getAuthority") {
    val getAuthorityResults = Array(
      "user` info@host",
      "user\u00DF\u00A3info@host:80",
      "user\u00DF\u00A3info@host:0",
// FIXME:
//      "user%60%20info@host:80",
//      "user%C3%9F%C2%A3info@host",
      "user\u00DF\u00A3info@host:80",
      "user` info@host:81",
      "user%info@host:0",
      null,
      null,
      null,
      null,
      "server.org",
      "reg:istry",
      null
    )
    (getUris zip getAuthorityResults).foreach {
      case (uri, expected) =>
        val actual = uri.getAuthority
        assert(actual == expected)
    }
  }

  // FIXME:
  ignore("getAuthority: regression test for HARMONY-1119") {
    assert(new URI(null, null, null, 127, null, null, null).getAuthority == null)
  }

  test("getAuthority2: tests for URIs with empty string authority component") {
    var uri = new URI("file:///tmp/")
    assert(uri.getAuthority == null)
    assert(uri.getHost == null)
    assert("file:///tmp/" == uri.toString)

    uri = new URI("file", "", "/tmp", "frag")
    assert(uri.getAuthority == null)
    assert(uri.getHost == null)
    assert("file:///tmp#frag" == uri.toString)

    uri = new URI("file", "", "/tmp", "query", "frag")
    assert(uri.getAuthority == null)
    assert(uri.getHost == null)
    assert("file:///tmp?query#frag" == uri.toString)

    // after normalization the host string info may be lost since the
    // uri string is reconstructed
    uri = new URI("file", "", "/tmp/a/../b/c", "query", "frag")
    val normalized = uri.normalize
    assert(uri.getAuthority == null)
    assert(uri.getHost == null)
    assert("file:///tmp/a/../b/c?query#frag" == uri.toString)
    assert("file:/tmp/b/c?query#frag" == normalized.toString)

    // the empty string host will give URISyntaxException for the 7 arg constructor
    assertThrows[URISyntaxException] {
      new URI("file", "user", "", 80, "/path", "query", "frag")
    }
  }

  test("getFragment") {
    val getFragmentResults = Array(
      "fr^ ag",
      "fr\u00E4\u00E8g",
      "fr\u00E4\u00E8g",
// FIXME
//      "fr%5E%20ag",
//      "fr%C3%A4%C3%A8g",
      "fr\u00E4\u00E8g",
      "fr^ ag",
      "f%rag",
      null,
      "",
      null,
      "fragment",
      null,
      null,
      null
    )
    (getUris zip getFragmentResults).foreach {
      case (uri, expected) =>
        assert(uri.getFragment == expected)
    }
  }

  test("getHost") {
    val getHostResults = Array(
      "host",
      "host",
      "host",
//      "host",
//      "host",
      "host",
      "host",
      "host",
      null,
      null,
      null,
      null,
      "server.org",
      null,
      null
    )
    (getUris zip getHostResults).foreach {
      case (uri, expected) =>
        assert(uri.getHost == expected)
    }
  }

  test("getPath") {
    val getPathResults = Array(
      "/a path",
      "/a\u20ACpath",
      "/a\u20ACpath",
//      "/a%20path",
//      "/a%E2%82%ACpath",
      "/a\u20ACpath",
      "/a path",
      "/a%path",
      null,
      "../adirectory/file.html",
      null,
      "",
      "",
      "",
      "/c:/temp/calculate.pl"
    )
    (getUris zip getPathResults).foreach {
      case (uri, expected) =>
        assert(uri.getPath == expected)
    }
  }

  test("getPort") {
    val getPortResults = Array(-1, 80, 0, /*80, -1,*/ 80, 81, 0, -1, -1, -1, -1, -1, -1, -1)
    (getUris zip getPortResults).foreach {
      case (uri, expected) =>
        assert(uri.getPort == expected)
    }
  }

  test("getPort2: if port value is negative, the authority should be consider registry based.") {
    var uri = new URI("http://myhost:-8096/site/index.html")
    assert(-1 == uri.getPort)
    assert(uri.getHost == null)
    assertThrows[URISyntaxException] {
      uri.parseServerAuthority
    }

    uri = new URI("http", "//myhost:-8096", null)
    assert(-1 == uri.getPort)
    assert(uri.getHost == null)
    assertThrows[URISyntaxException] {
      uri.parseServerAuthority
    }
  }

  test("getQuery") {
    val getQueryResults = Array(
      "qu` ery",
      "qu\u00A9\u00AEery",
      "qu\u00A9\u00AEery",
//      "qu%60%20ery",
//      "qu%C2%A9%C2%AEery",
      "qu\u00A9\u00AEery",
      "qu` ery",
      "que%ry",
      null,
      null,
      null,
      null,
      null,
      "query",
      ""
    )
    (getUris zip getQueryResults).foreach {
      case (uri, expected) =>
        assert(uri.getQuery == expected)
    }
  }

  test("getRawAuthority") {
    val getRawAuthorityResults = Array(
      "user%60%20info@host",
      "user%C3%9F%C2%A3info@host:80",
      "user\u00DF\u00A3info@host:0",
//      "user%2560%2520info@host:80",
//      "user%25C3%259F%25C2%25A3info@host",
      "user\u00DF\u00A3info@host:80",
      "user%60%20info@host:81",
      "user%25info@host:0",
      null,
      null,
      null,
      null,
      "server.org",
      "reg:istry",
      null
    )
    (getUris zip getRawAuthorityResults).foreach {
      case (uri, expected) =>
        val result = uri.getRawAuthority
        assert(result == expected)
    }
  }

  def test_getRawFragment(): Unit = {
    val getRawFragmentResults = Array(
      "fr%5E%20ag",
      "fr%C3%A4%C3%A8g",
      "fr\u00E4\u00E8g",
//      "fr%255E%2520ag",
//      "fr%25C3%25A4%25C3%25A8g",
      "fr\u00E4\u00E8g",
      "fr%5E%20ag",
      "f%25rag",
      null,
      "",
      null,
      "fragment",
      null,
      null,
      null
    )
    (getUris zip getRawFragmentResults).foreach {
      case (uri, expected) =>
        assert(uri.getRawFragment == expected)
    }
  }

  test("getRawPath") {
    val getRawPathResults = Array(
      "/a%20path",
      "/a%E2%82%ACpath",
      "/a\u20ACpath",
//      "/a%2520path",
//      "/a%25E2%2582%25ACpath",
      "/a\u20ACpath",
      "/a%20path",
      "/a%25path",
      null,
      "../adirectory/file.html",
      null,
      "",
      "",
      "",
      "/c:/temp/calculate.pl"
    )
    (getUris zip getRawPathResults).foreach {
      case (uri, expected) =>
        assert(uri.getRawPath == expected)
    }
  }

  test("getRawQuery") {
    val getRawQueryResults = Array(
      "qu%60%20ery",
      "qu%C2%A9%C2%AEery",
      "qu\u00A9\u00AEery",
//      "qu%2560%2520ery",
//      "qu%25C2%25A9%25C2%25AEery",
      "qu\u00A9\u00AEery",
      "qu%60%20ery",
      "que%25ry",
      null,
      null,
      null,
      null,
      null,
      "query",
      ""
    )
    (getUris zip getRawQueryResults).foreach {
      case (uri, expected) =>
        assert(uri.getRawQuery == expected)
    }
  }

  // FIXME
  ignore("getRawSchemeSpecificPart") {
    val getRawSspResults = Array(
      "//user%60%20info@host/a%20path?qu%60%20ery",
      "//user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery",
      "//user\u00DF\u00A3info@host:0/a\u20ACpath?qu\u00A9\u00AEery",
//      "//user%2560%2520info@host:80/a%2520path?qu%2560%2520ery",
//      "//user%25C3%259F%25C2%25A3info@host/a%25E2%2582%25ACpath?qu%25C2%25A9%25C2%25AEery",
      "//user\u00DF\u00A3info@host:80/a\u20ACpath?qu\u00A9\u00AEery",
      "//user%60%20info@host:81/a%20path?qu%60%20ery",
      "//user%25info@host:0/a%25path?que%25ry",
      "user@domain.com",
      "../adirectory/file.html",
      "comp.infosystems.www.servers.unix",
      "",
      "//server.org",
      "//reg:istry?query",
      "///c:/temp/calculate.pl?"
    )
    (getUris zip getRawSspResults).foreach {
      case (uri, expected) =>
        assert(uri.getRawSchemeSpecificPart == expected)
    }
  }

  test("getRawUserInfo") {
    val getRawUserInfoResults = Array(
      "user%60%20info",
      "user%C3%9F%C2%A3info",
      "user\u00DF\u00A3info",
//      "user%2560%2520info",
//      "user%25C3%259F%25C2%25A3info",
      "user\u00DF\u00A3info",
      "user%60%20info",
      "user%25info",
      null,
      null,
      null,
      null,
      null,
      null,
      null
    )
    (getUris zip getRawUserInfoResults).foreach {
      case (uri, expected) =>
        assert(uri.getRawUserInfo == expected)
    }
  }

  test("getScheme") {
    val getSchemeResults = Array(
      "http",
      "http",
      "ascheme",
//      "http",
//      "http",
      "ascheme",
      "http",
      "http",
      "mailto",
      null,
      "news",
      null,
      "telnet",
      "http",
      "file"
    )
    (getUris zip getSchemeResults).foreach {
      case (uri, expected) =>
        assert(uri.getScheme == expected)
    }
  }

  test("getSchemeSpecificPart") {
    val getSspResults = Array(
      "//user` info@host/a path?qu` ery",
      "//user\u00DF\u00A3info@host:80/a\u20ACpath?qu\u00A9\u00AEery",
      "//user\u00DF\u00A3info@host:0/a\u20ACpath?qu\u00A9\u00AEery",
//      "//user%60%20info@host:80/a%20path?qu%60%20ery",
//      "//user%C3%9F%C2%A3info@host/a%E2%82%ACpath?qu%C2%A9%C2%AEery",
      "//user\u00DF\u00A3info@host:80/a\u20ACpath?qu\u00A9\u00AEery",
      "//user` info@host:81/a path?qu` ery",
      "//user%info@host:0/a%path?que%ry",
      "user@domain.com",
      "../adirectory/file.html",
      "comp.infosystems.www.servers.unix",
      "",
      "//server.org",
      "//reg:istry?query",
      "///c:/temp/calculate.pl?"
    )
    (getUris zip getSspResults).foreach {
      case (uri, expected) =>
        assert(uri.getSchemeSpecificPart == expected)
    }
  }

  test("getUserInfo") {
    val getUserInfoResults = Array(
      "user` info",
      "user\u00DF\u00A3info",
      "user\u00DF\u00A3info",
//      "user%60%20info",
//      "user%C3%9F%C2%A3info",
      "user\u00DF\u00A3info",
      "user` info",
      "user%info",
      null,
      null,
      null,
      null,
      null,
      null,
      null
    )
    (getUris zip getUserInfoResults).foreach {
      case (uri, expected) =>
        assert(uri.getUserInfo == expected)
    }
  }

  test("hashCode") {
    val hashCodeData = Array[(String, String, Boolean)](
      ("", "", true), // null frags
      ("/path", "/path#frag", false),
      ("#frag", "#frag2", false),
      ("#frag", "#FRag", false),
      ("#fr%4F", "#fr%4F", true),             // case insensitive on hex escapes
      ("scheme:test", "scheme2:test", false), // scheme
      ("test", "http:test", false),
      ("http:test", "test", false),         // case insensitivity for scheme
      ("SCheme:test", "schEMe:test", true), // hierarchical/opaque mismatch
      ("mailto:jim", "mailto://jim", false),
      ("mailto://test", "mailto:test", false), // opaque
      ("mailto:name", "mailto:name", true),
      ("mailtO:john", "mailto:john", true),
      ("mailtO:john", "mailto:johN", false),
      ("mailtO:john", "mailto:jim", false),
      ("mailto:te%4Fst", "mailto:te%4Fst", true),
      ("mailto:john#frag", "mailto:john#frag2", false), // hierarchical
      ("/test/", "/test/", true),                       // paths
      ("/te%F4st", "/te%F4st", true),
      ("/TEst", "/teSt", false),
      ("", "/test", false), // registry based because they don't resolve properly to

      // server-based
      // add more tests here
      ("//host.com:80err", "//host.com:80e", false),
      ("//host.com:81e%Abrr", "//host.com:81e%Abrr", true),
      ("//Host.com:80e", "//hoSt.com:80e", false),
      ("/test", "//auth.com/test", false),
      ("//test.com", "/test", false),
      ("//test.com", "//test.com", true), // server based

      // case insensitivity for host
      ("//HoSt.coM/", "//hOsT.cOm/", true),
      ("//te%aE.com", "//te%aE.com", true),
      ("//test.com:80", "//test.com:81", false),
      ("//joe@test.com:80", "//test.com:80", false),
      ("//jo%3E@test.com:82", "//jo%3E@test.com:82", true),
      ("//test@test.com:85", "//test@test.com", false)
    )
    hashCodeData.foreach {
      case (x, y, z) =>
        val b = new URI(x)
        val r = new URI(y)
        withClue(s"For $x and $y") {
          assert((b.hashCode == r.hashCode) == z)
        }
    }
  }

  test("isAbsolute") {
    val isAbsoluteData = Array[(String, Boolean)](
      ("mailto:user@ca.ibm.com", true),
      ("urn:isbn:123498989h", true),
      ("news:software.ibm.com", true),
      ("http://www.amazon.ca", true),
      ("file:///d:/temp/results.txt", true),
      ("scheme:ssp", true),
      ("calculate.pl?isbn=123498989h", false),
      ("?isbn=123498989h", false),
      ("//www.amazon.ca", false),
      ("a.html", false),
      ("#top", false),
      ("//pc1/", false),
      ("//user@host/path/file", false)
    )
    isAbsoluteData.foreach {
      case (x, expected) =>
        withClue(s"for $x") {
          assert(new URI(x).isAbsolute == expected)
        }
    }
  }

  test("isOpaque") {
    val isOpaqueData = Array[(String, Boolean)](
      ("mailto:user@ca.ibm.com", true),
      ("urn:isbn:123498989h", true),
      ("news:software.ibm.com", true),
      ("http://www.amazon.ca", false),
      ("file:///d:/temp/results.txt", false),
      ("scheme:ssp", true),
      ("calculate.pl?isbn=123498989h", false),
      ("?isbn=123498989h", false),
      ("//www.amazon.ca", false),
      ("a.html", false),
      ("#top", false),
      ("//pc1/", false),
      ("//user@host/path/file", false)
    )
    isOpaqueData.foreach {
      case (x, expected) =>
        withClue(s"for $x") {
          assert(new URI(x).isOpaque == expected)
        }
    }
  }

  test("normalize") {
    val normalizeData = Array[(String, String)](
      // normal
      ("/", "/"),
      ("/a", "/a"),
      ("/a/b", "/a/b"),
      ("/a/b/c", "/a/b/c"),
      // single,  '.'
      ("/.", "/"),
      ("/./", "/"),
      ("/./.", "/"),
      ("/././", "/"),
      ("/./a", "/a"),
      ("/./a/", "/a/"),
      ("/././a", "/a"),
      ("/././a/", "/a/"),
      ("/a/.", "/a/"),
      ("/a/./", "/a/"),
      ("/a/./.", "/a/"),
      ("/a/./b", "/a/b"),
      // double, '..'
      ("/a/..", "/"),
      ("/a/../", "/"),
      ("/a/../b", "/b"),
      ("/a/../b/..", "/"),
      ("/a/../b/../", "/"),
      ("/a/../b/../c", "/c"),
      ("/..", "/.."),
      ("/../", "/../"),
      ("/../..", "/../.."),
      ("/../../", "/../../"),
      ("/../a", "/../a"),
      ("/../a/", "/../a/"),
      ("/../../a", "/../../a"),
      ("/../../a/", "/../../a/"),
      ("/a/b/../../c", "/c"),
      ("/a/b/../..", "/"),
      ("/a/b/../../", "/"),
      ("/a/b/../../c", "/c"),
      ("/a/b/c/../../../d", "/d"),
      ("/a/b/..", "/a/"),
      ("/a/b/../", "/a/"),
      ("/a/b/../c", "/a/c"),
      // miscellaneous
      ("/a/b/.././../../c/./d/../e", "/../c/e"),
      ("/a/../../.c././../././c/d/../g/..", "/../c/"),
      // '.' in the middle of segments
      ("/a./b", "/a./b"),
      ("/.a/b", "/.a/b"),
      ("/a.b/c", "/a.b/c"),
      ("/a/b../c", "/a/b../c"),
      ("/a/..b/c", "/a/..b/c"),
      ("/a/b..c/d", "/a/b..c/d"),
      // no leading slash, miscellaneous
      ("", ""),
      ("a", "a"),
      ("a/b", "a/b"),
      ("a/b/c", "a/b/c"),
      ("../", "../"),
// FIXME
//      (".", ""),
      ("..", ".."),
      ("../g", "../g")
//      ("g/a/../../b/c/./g", "b/c/g"),
//      ("a/b/.././../../c/./d/../e", "../c/e"),
//      ("a/../../.c././../././c/d/../g/..", "../c/")
    )
    normalizeData.foreach {
      case (x, expected) =>
        withClue(s"for $x") {
          try {
            assert(new URI(x).normalize().toString == expected)
          } catch {
            case e: Exception => throw new Exception(x, e)
          }

        }
    }
  }

  test("normalize2: windows drive letter") {
    val uri1 = new URI("file:/D:/one/two/../../three")
    val uri2 = uri1.normalize
    assert("file:/D:/three" == uri2.toString)
    assert(uri2.isAbsolute)
    assert(!uri2.isOpaque)
    assert("/D:/three" == uri2.getRawSchemeSpecificPart)
  }

  test("normalize3: return same URI if it has a normalized path already") {
    var uri1 = new URI("http://host/D:/one/two/three")
    var uri2 = uri1.normalize
    assert(uri1 eq uri2)
    // try with empty path
    uri1 = new URI("http", "host", null, "fragment")
    uri2 = uri1.normalize
    assert(uri1 eq uri2)
  }

  test("parseServerAuthority") {
    // registry based uris
    val uris = Array[URI](
      // port number not digits
      new URI("http://foo:bar/file#fragment"),
      new URI("http", "//foo:bar/file", "fragment"),
      // unicode char in the hostname = new URI("http://host\u00dfname/")
      new URI("http://host\u00DFname/"),
      new URI("http", "//host\u00DFname/", null),
      // = new URI("http://host\u00dfname/", null),
      // escaped octets in host name
      new URI("http://host%20name/"),
      new URI("http", "//host%20name/", null),
      // missing host name, port number
      new URI("http://joe@:80"),
      // missing host name, no port number
      new URI("http://user@/file?query#fragment"),
      new URI("//host.com:80err"),
      // malformed port number
      new URI("//host.com:81e%Abrr"),
      // malformed ipv4 address
      new URI("telnet", "//256.197.221.200", null),
      new URI("telnet://198.256.221.200"),
      new URI("//te%ae.com"),
      // misc ..
      new URI("//:port"),
      new URI("//:80"),
      // last label has to start with alpha char
      new URI("//fgj234fkgj.jhj.123."),
      new URI("//fgj234fkgj.jhj.123"),
      // '-' cannot be first or last character in a label
      new URI("//-domain.name"),
      new URI("//domain.name-"),
      new URI("//domain-"),
      // illegal char in host name
      new URI("//doma*in"),
      // host expected
      new URI("http://:80/"),
      new URI("http://user@/"),
      // ipv6 address not enclosed in "[]"
      new URI("http://3ffe:2a00:100:7031:22:1:80:89/"),
      // expected ipv6 addresses to be enclosed in "[]"
      new URI("http", "34::56:78", "/path", "query", "fragment"),
      // expected host
      new URI("http", "user@", "/path", "query", "fragment")
    )

    // these URIs do not have valid server based authorities,
    // but single arg, 3 and 5 arg constructors
    // parse them as valid registry based authorities
    // exception should occur when parseServerAuthority is
    uris.foreach { uri =>
      assertThrows[URISyntaxException] {
        uri.parseServerAuthority
      }
    }

    // valid Server based authorities
    new URI("http", "3ffe:2a00:100:7031:2e:1:80:80", "/path", "fragment").parseServerAuthority
    new URI("http", "host:80", "/path", "query", "fragment").parseServerAuthority
    new URI("http://[::3abc:4abc]:80/").parseServerAuthority
    new URI("http", "34::56:78", "/path", "fragment").parseServerAuthority
    new URI("http", "[34:56::78]:80", "/path", "fragment").parseServerAuthority

    // invalid authorities (neither server nor registry)
    assertThrows[URISyntaxException] {
      new URI("http://us[er@host:80/")
    }
    assertThrows[URISyntaxException] {
      new URI("http://[ddd::hgghh]/")
    }
    assertThrows[URISyntaxException] {
      new URI("http", "[3ffe:2a00:100:7031:2e:1:80:80]a:80", "/path", "fragment")
    }
  }

  ignore("parseServerAuthority: temporary disabled") {
    assertThrows[URISyntaxException] {
      new URI("http", "host:80", "/path", "fragment")
    }
    // regression test for HARMONY-1126
    assert(URI.create("file://C:/1.txt").parseServerAuthority != null)
  }

  test("relativizeLjava_net_URI") {
    val relativizeData = Array[(String, String, String)](
      // 1st is base, 2nd is the one to relativize, 3rd is expected
      ("http://www.google.com/dir1/dir2", "mailto:test", "mailto:test"), // rel =
      // opaque
      ("mailto:test", "http://www.google.com", "http://www.google.com"), // base = opaque
      // different authority
      (
        "http://www.eclipse.org/dir1",
        "http://www.google.com/dir1/dir2",
        "http://www.google.com/dir1/dir2"
      ),
      // different scheme
      ("http://www.google.com", "ftp://www.google.com", "ftp://www.google.com"),
      (
        "http://www.google.com/dir1/dir2/",
        "http://www.google.com/dir3/dir4/file.txt",
        "http://www.google.com/dir3/dir4/file.txt"
      )
// FIXME
//      ("http://www.google.com/dir1/", "http://www.google.com/dir1/dir2/file.txt", "dir2/file.txt"),
//      ("./dir1/", "./dir1/hi", "hi"),
//      ("/dir1/./dir2", "/dir1/./dir2/hi", "hi"),
//      ("/dir1/dir2/..", "/dir1/dir2/../hi", "hi"),
//      ("/dir1/dir2/..", "/dir1/dir2/hi", "dir2/hi"),
//      ("/dir1/dir2/", "/dir1/dir3/../dir2/text", "text"),
//      ("//www.google.com", "//www.google.com/dir1/file", "dir1/file"),
//      ("/dir1", "/dir1/hi", "hi"),
//      ("/dir1/", "/dir1/hi", "hi")
    )
    relativizeData.foreach {
      case (x, y, expected) =>
        withClue(s"For $x and $y") {
          try {
            val b = new URI(x)
            val r = new URI(y)
            assert(b.relativize(r).toString == expected)
            assert(b.relativize(r) == new URI(expected))
          } catch {
            case e: URISyntaxException =>
              fail(s"Reason ${e.getMessage}")
          }
        }
    }

    var a = new URI("http://host/dir")
    var b = new URI("http://host/dir/file?query")
// FIXME
//    assert(new URI("file?query") == a.relativize(b))

    // One URI with empty host
    a = new URI("file:///~/first")
    b = new URI("file://tools/~/first")
    assert(new URI("file://tools/~/first") == a.relativize(b))
    assert(new URI("file:///~/first") == b.relativize(a))

    // Both URIs with empty hosts
    b = new URI("file:///~/second")
    assert(new URI("file:///~/second") == a.relativize(b))
    assert(new URI("file:///~/first") == b.relativize(a))
  }

  test("relativize2") {
    var a = new URI("http://host/dir")
    var b = new URI("http://host/dir/file?query")
// FIXME
//    assert(new URI("file?query") == a.relativize(b))

    // one URI with empty host
    a = new URI("file:///~/dictionary")
    b = new URI("file://tools/~/dictionary")
    assert(new URI("file://tools/~/dictionary") == a.relativize(b))
    assert(new URI("file:///~/dictionary") == b.relativize(a))

    // two URIs with empty hosts
    b = new URI("file:///~/therasus")
    assert(new URI("file:///~/therasus") == a.relativize(b))
    assert(new URI("file:///~/dictionary") == b.relativize(a))
    var one   = new URI("file:/C:/test/ws")
    var two   = new URI("file:/C:/test/ws")
    val empty = new URI("")
//    assert(empty == one.relativize(two))

    one = new URI("file:/C:/test/ws")
    two = new URI("file:/C:/test/ws/p1")
    val result = new URI("p1")
//    assert(result == one.relativize(two))
    one = new URI("file:/C:/test/ws/")
//    assert(result == one.relativize(two))
  }

  // FIXME
  ignore("relativize3: Regression test for HARMONY-6075") {
    val uri      = new URI("file", null, "/test/location", null)
    val base     = new URI("file", null, "/test", null)
    val relative = base.relativize(uri)
    assert("location" == relative.getSchemeSpecificPart)
    assert(relative.getScheme == null)
  }

  test("resolveLjava_net_URI") {
    val resolveData = Array[(String, String, String)](
      // authority in given URI
      (
        "http://www.test.com/dir",
        "//www.test.com/hello?query#fragment",
        "http://www.test.com/hello?query#fragment"
      ),
      // no authority, absolute path
      ("http://www.test.com/dir", "/abspath/file.txt", "http://www.test.com/abspath/file.txt"),
      // no authority, relative paths
      ("/", "dir1/file.txt", "/dir1/file.txt"),
      ("/dir1", "dir2/file.txt", "/dir2/file.txt"),
      ("/dir1/", "dir2/file.txt", "/dir1/dir2/file.txt"),
// FIXME
//      ("", "dir1/file.txt", "dir1/file.txt"),
//      ("dir1", "dir2/file.txt", "dir2/file.txt"),
//      ("dir1/", "dir2/file.txt", "dir1/dir2/file.txt"),
      // normalization required
      ("/dir1/dir2/../dir3/./", "dir4/./file.txt", "/dir1/dir3/dir4/file.txt"),
      // allow a standalone fragment to be resolved
      (
        "http://www.google.com/hey/joe?query#fragment",
        "#frag2",
        "http://www.google.com/hey/joe?query#frag2"
      ),
      // return given when base is opaque
      ("mailto:idontexist@uk.ibm.com", "dir1/dir2", "dir1/dir2"),
      // return given when given is absolute
      ("http://www.google.com/hi/joe", "http://www.oogle.com", "http://www.oogle.com")
    )
    resolveData.foreach {
      case (x, y, expected) =>
        withClue(s"For $x and $y") {
          try {
            val b      = new URI(x)
            val r      = new URI(y)
            val result = b.resolve(r)
            assert(result.toString == expected)
            assert(b.isOpaque || b.isAbsolute == result.isAbsolute)
          } catch {
            case e: URISyntaxException => fail(s"Reason ${e.getMessage}")
          }
        }
    }
  }

  test("resolve2") {
    val uri1 = new URI("file:/D:/one/two/three")
    val uri2 = uri1.resolve(new URI(".."))
    assert("file:/D:/one/" == uri2.toString)
    assert(uri2.isAbsolute)
    assert(!uri2.isOpaque)
    assert("/D:/one/" == uri2.getRawSchemeSpecificPart)
  }

  // FIXME
  ignore("toASCIIString") {
    val toASCIIStringResults0 = Array[String](
      "http://user%60%20info@host/a%20path?qu%60%20ery#fr%5E%20ag",
      "http://user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g",
      "ascheme://user%C3%9F%C2%A3info@host:0/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g",
      //      "http://user%2560%2520info@host:80/a%2520path?qu%2560%2520ery#fr%255E%2520ag",
      //      "http://user%25C3%259F%25C2%25A3info@host/a%25E2%2582%25ACpath?qu%25C2%25A9%25C2%25AEery#fr%25C3%25A4%25C3%25A8g",
      "ascheme://user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g",
      "http://user%60%20info@host:81/a%20path?qu%60%20ery#fr%5E%20ag",
      "http://user%25info@host:0/a%25path?que%25ry#f%25rag",
      "mailto:user@domain.com",
      "../adirectory/file.html#",
      "news:comp.infosystems.www.servers.unix",
      "#fragment",
      "telnet://server.org",
      "http://reg:istry?query",
      "file:///c:/temp/calculate.pl?"
    )
    (getUris zip toASCIIStringResults0).foreach {
      case (uri, expected) =>
        assert(uri.toASCIIString == expected)
    }
  }

  test("toASCIIString2") {
    val toASCIIStringData = Array[String](
      "http://www.test.com/\u00DF/dir/",
      "http://www.test.com/\u20AC/dir",
      "http://www.\u20AC.com/dir",
      "http://www.test.com/\u20AC/dir/file#fragment",
      "mailto://user@domain.com",
      "mailto://user\u00DF@domain.com"
    )
    val toASCIIStringResults = Array[String](
      "http://www.test.com/%C3%9F/dir/",
      "http://www.test.com/%E2%82%AC/dir",
      "http://www.%E2%82%AC.com/dir",
      "http://www.test.com/%E2%82%AC/dir/file#fragment",
      "mailto://user@domain.com",
      "mailto://user%C3%9F@domain.com"
    )
    (toASCIIStringData zip toASCIIStringResults).foreach {
      case (uri, expected) =>
        assert(new URI(uri).toASCIIString == expected)
    }
  }

  ignore("toString") {
    val toStringResults = Array(
      "http://user%60%20info@host/a%20path?qu%60%20ery#fr%5E%20ag",
      "http://user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g",
      "ascheme://user\u00DF\u00A3info@host:0/a\u20ACpath?qu\u00A9\u00AEery#fr\u00E4\u00E8g",
//      "http://user%2560%2520info@host:80/a%2520path?qu%2560%2520ery#fr%255E%2520ag",
//      "http://user%25C3%259F%25C2%25A3info@host/a%25E2%2582%25ACpath?qu%25C2%25A9%25C2%25AEery#fr%25C3%25A4%25C3%25A8g",
      "ascheme://user\u00DF\u00A3info@host:80/a\u20ACpath?qu\u00A9\u00AEery#fr\u00E4\u00E8g",
      "http://user%60%20info@host:81/a%20path?qu%60%20ery#fr%5E%20ag",
      "http://user%25info@host:0/a%25path?que%25ry#f%25rag",
      "mailto:user@domain.com",
      "../adirectory/file.html#",
      "news:comp.infosystems.www.servers.unix",
      "#fragment",
      "telnet://server.org",
      "http://reg:istry?query",
      "file:///c:/temp/calculate.pl?"
    )
    (getUris zip toStringResults).foreach {
      case (uri, expected) =>
        assert(uri.toString == expected)
    }
  }

  test("toURL") {
    assume(!isScalaJS, "URI.toURL is not implemented yet")
    val absoluteUrisCanBeUrl = Array[String](
      "mailto:noreply@apache.org",
      "http://www.apache.org",
      "file:///d:/temp/results.txt"
    )
    for (uri <- absoluteUrisCanBeUrl) {
      new URI(uri).toURL
    }

    val absoluteUris = Array[String](
      "urn:isbn:123498989h",
      "news:software.ibm.com",
      "scheme:ssp"
    )
    for (uri <- absoluteUris) {
      assertThrows[MalformedURLException] {
        new URI(uri).toURL
      }
    }

    val relativeuris = Array[String](
      "calculate.pl?isbn=123498989h",
      "?isbn=123498989h",
      "//www.apache.org",
      "a.html",
      "#top",
      "//pc1/",
      "//user@host/path/file"
    )
    for (uri <- relativeuris) {
      assertThrows[IllegalArgumentException] {
        new URI(uri).toURL
      }
    }
  }

  ignore("SerializationSelf") {
    val uri = new URI("http://harmony.apache.org/")
    // TODO
  }
}
