$version: "2"

namespace alloy.common

/// Email address as defined in https://www.rfc-editor.org/rfc/rfc2821 and https://www.rfc-editor.org/rfc/rfc2822
/// A more human-readable format is available here: https://www.rfc-editor.org/rfc/rfc3696#section-3
@trait(selector: ":test(string, member > string)")
structure emailFormat { }

/// ISO 3166-1 Alpha-2 country code
/// Full list in https://www.iso.org/obp/ui/#search/code/
/// example: "AF", "US"
@trait(selector: ":test(string, member > string)")
structure countryCodeFormat { }

/// ISO 639-1 Alpha-2 language code or Language for short.
/// Column `ISO 639-1` in https://www.loc.gov/standards/iso639-2/php/English_list.php
/// example: "fr", "en"
@trait(selector: ":test(string, member > string)")
structure languageCodeFormat { }

/// BCP 47 Language Tag
/// IETF RFC: https://tools.ietf.org/search/bcp47
/// example: "fr-CA", "en-US"
@trait(selector: ":test(string, member > string)")
structure languageTagFormat { }

/// A hex triplet representing a RGB color code
/// example: "#09C" (short) or "#0099CC" (full)
@trait(selector: ":test(string, member > string)")
structure hexColorCodeFormat { }

/// IP Address, supporting both v4 and v6 addresses
/// IETF RFC: https://www.rfc-editor.org/rfc/rfc791
///   v6 RFC: https://www.rfc-editor.org/rfc/rfc1883
/// example: "192.168.1.1", "::1"
@trait(selector: ":test(string, member > string)")
structure ipaddressFormat { }

/// IP Address range using CIDR 
/// IETF RFC: https://www.rfc-editor.org/rfc/rfc1817
/// example: "192.0.2.0/24", "2001:db8::/32"
@trait(selector: ":test(string, member > string)")
structure cidrFormat { }
