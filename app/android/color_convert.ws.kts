val input = """
    Neutral / Light / 900
    33,33,33,1
    Neutral / Black
    0,0,0,1
    Neutral / White
    255,255,255,1
    Green/ Dark / 100
    34,84,61,0.5
    Green/ Dark / 200
    34,84,61,1
    Green/ Dark / 300
    39,103,73,1
    Green/ Dark / 400
    47,133,90,1
    Green/ Dark / 500
    56,161,105,1
    Green/ Dark / 600
    72,187,120,1
    Green/ Dark / 700
    104,211,145,1
    Green/ Dark / 800
    154,230,180,1
    Green/ Dark / 900
    198,246,213,1
    Green/ Light / 100
    235,255,240,1
    Green/ Light / 200
    198,246,213,1
    Green/ Light / 300
    154,230,180,1
    Green/ Light / 400
    104,211,145,1
    Green/ Light / 500
    72,187,120,1
    Green/ Light / 600
    56,161,105,1
    Green/ Light / 700
    47,133,90,1
    Green/ Light / 800
    39,103,73,1
    Green/ Light / 900
    34,84,61,1
    Neutral / Dark / 100
    66,66,66,1
    Neutral / Dark / 200
    97,97,97,1
    Neutral / Dark / 300
    117,117,117,1
    Neutral / Dark / 400
    158,158,158,1
    Neutral / Dark / 50
    33,33,33,1
    Neutral / Dark / 500
    189,189,189,1
    Neutral / Dark / 600
    224,224,224,1
    Neutral / Dark / 700
    238,238,238,1
    Neutral / Dark / 800
    245,245,245,1
    Neutral / Dark / 900
    250,250,250,1
    Neutral / Light / 100
    245,245,245,1
    Neutral / Light / 200
    238,238,238,1
    Neutral / Light / 300
    224,224,224,1
    Neutral / Light / 400
    189,189,189,1
    Neutral / Light / 50
    250,250,250,1
    Neutral / Light / 500
    158,158,158,1
    Neutral / Light / 600
    117,117,117,1
    Neutral / Light / 700
    97,97,97,1
    Neutral / Light / 800
    66,66,66,1
    Primary / Dark / 100
    51,81,122,0.5
    Primary / Dark / 200
    42,67,101,1
    Primary / Dark / 300
    44,82,130,1
    Primary / Dark / 400
    43,108,176,1
    Primary / Dark / 500
    49,130,206,1
    Primary / Dark / 600
    66,153,225,1
    Primary / Dark / 700
    99,179,237,1
    Primary / Dark / 800
    144,205,244,1
    Primary / Dark / 900
    190,227,248,1
    Primary / Light / 100
    235,248,255,1
    Primary / Light / 200
    190,227,248,1
    Primary / Light / 300
    144,205,244,1
    Primary / Light / 400
    99,179,237,1
    Primary / Light / 500
    66,153,225,1
    Primary / Light / 600
    49,130,206,1
    Primary / Light / 700
    43,108,176,1
    Primary / Light / 800
    44,82,130,1
    Primary / Light / 900
    42,67,101,1
    Red / Dark / 100
    116,42,42,0.5
    Red / Dark / 200
    116,42,42,1
    Red / Dark / 300
    155,44,44,1
    Red / Dark / 400
    197,48,48,1
    Red / Dark / 500
    229,62,62,1
    Red / Dark / 600
    245,101,101,1
    Red / Dark / 700
    252,129,129,1
    Red / Dark / 800
    254,178,178,1
    Red / Dark / 900
    254,215,215,1
    Red / Light / 100
    255,235,235,1
    Red / Light / 200
    254,215,215,1
    Red / Light / 300
    254,178,178,1
    Red / Light / 400
    252,129,129,1
    Red / Light / 500
    245,101,101,1
    Red / Light / 600
    229,62,62,1
    Red / Light / 700
    197,48,48,1
    Red / Light / 800
    155,44,44,1
    Red / Light / 900
    116,42,42,1
    Transparent Overlays / Dark Mode / 15%
    255,255,255,0.15
    Transparent Overlays / Dark Mode / 60%
    255,255,255,0.6
    Transparent Overlays / Light Mode / 15%
    0,0,0,0.15
    Transparent Overlays / Light Mode / 60%
    0,0,0,0.6
    Yellow / Dark / 100
    236,201,75,0.3
    Yellow / Dark / 200
    116,66,16,1
    Yellow / Dark / 300
    151,90,22,1
    Yellow / Dark / 400
    183,121,31,1
    Yellow / Dark / 500
    214,158,46,1
    Yellow / Dark / 600
    236,201,75,1
    Yellow / Dark / 700
    246,224,94,1
    Yellow / Dark / 800
    250,240,137,1
    Yellow / Dark / 900
    254,252,191,1
    Yellow / Light / 100
    255,255,235,1
    Yellow / Light / 200
    254,252,191,1
    Yellow / Light / 300
    250,240,137,1
    Yellow / Light / 400
    246,224,94,1
    Yellow / Light / 500
    236,201,75,1
    Yellow / Light / 600
    214,158,46,1
    Yellow / Light / 700
    183,121,31,1
    Yellow / Light / 800
    151,90,22,1
    Yellow / Light / 900
    116,66,16,1
    Neutral / Light / 900
    33,33,33,1
    Elevation / Full Color / 00dp
    33,33,33,1
    Elevation / Full Color / 01dp
    44,44,44,1
    Elevation / Full Color / 02dp
    49,49,49,1
    Elevation / Full Color / 03dp
    50,50,50,1
    Elevation / Full Color / 04dp
    53,53,53,1
    Elevation / Full Color / 06dp
    57,57,57,1
    Elevation / Full Color / 08dp
    60,60,60,1
    Elevation / Full Color / 12dp
    64,64,64,1
    Elevation / Full Color / 16dp
    66,66,66,1
    Elevation / Full Color / 24dp
    69,69,69,1
""".trimIndent()

input.split("\n").windowed(2, 2) {
    val name = it[0]
        .replace("/", "")
        .replace("  ", " ")
        .replace(" ", "_")
        .replace("%", "p")
        .toLowerCase()
    val rgba = it[1].split(",")
    var color = "#%02X%02X%02X%02X".format(
        (rgba[3].toFloat() * 255).toInt(),
        rgba[0].toInt(),
        rgba[1].toInt(),
        rgba[2].toInt(),
    )
}

input.split("\n").windowed(2, 2) {
    var name = it[0]
        .replace("/", "")
        .replace(" ", "")
        .replace("%", "p")

    name = "${name[0].toLowerCase()}${name.subSequence(1, name.length)}"

    val rgba = it[1].split(",")
    var color = "Color(0x%02X%02X%02X%02X)".format(
        (rgba[3].toFloat() * 255).toInt(),
        rgba[0].toInt(),
        rgba[1].toInt(),
        rgba[2].toInt(),
    )
}

input.split("\n").windowed(2, 2) {
    var name = it[0]
        .replace("/", "")
        .replace(" ", "")
        .replace("%", "p")

    name = "${name[0].toLowerCase()}${name.subSequence(1, name.length)}"

    val rgba = it[1].split(",")
    var color = "Color(0x%02X%02X%02X%02X)".format(
        (rgba[3].toFloat() * 255).toInt(),
        rgba[0].toInt(),
        rgba[1].toInt(),
        rgba[2].toInt(),
    )
}

input.split("\n").windowed(2, 2) {
    var name = it[0]
        .replace("/", "")
        .replace(" ", "")
        .replace("%", "p")

    name = "${name[0].toLowerCase()}${name.subSequence(1, name.length)}"
}
