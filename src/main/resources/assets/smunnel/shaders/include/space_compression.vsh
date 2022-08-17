uniform float tunnelStart; // Lowest coordinate of the tunnel
uniform int tunnelData; // Length, axis, and data about the position relative to the camera
uniform float tunnelMultiplicationFactor;

void smunnelCompressVertex(inout vec3 pos) {
    int tunnelLength = (tunnelData & 0x0fffffff) - (134217727); // 0b00001111 11111111 11111111 11111111
    float tunnelEnd = tunnelStart + tunnelLength;
    // 01: x
    // 10: y
    // 11: z
    int tunnelAxis = tunnelData & 0x30000000;                   // 0b00110000 00000000 00000000 00000000
    // 00: disabled
    // 01: tunnel on the positive coordinates
    // 10: tunnel on the negative coordinates
    // 11: tunnel starts negative, ends positive
    int boundMode = tunnelData & 0xC0000000;                    // 0b11000000 00000000 00000000 00000000

    float coordinate = 0;
    switch (tunnelAxis) {
        case 0x10000000:
            coordinate = pos.x;
            break;
        case 0x20000000:
            coordinate = pos.y;
            break;
        case 0x30000000:
            coordinate = pos.z;
            break;
    }

    // Everything is centered on 0,0,0 which is where the camera is
    // The tunnel start and end values are already converted to these coordinates
    float inTunnel;
    float postTunnel;
    float preTunnel;
    switch (boundMode) {
        case 0x00000000:
            preTunnel = coordinate;
            inTunnel = 0;
            postTunnel = 0;
            break;
        case 0x40000000:
            preTunnel = min(coordinate, tunnelStart);
            inTunnel = clamp(coordinate - tunnelStart, 0, tunnelLength);
            postTunnel = max(0, coordinate - tunnelEnd);
            break;
        case 0x80000000:
            preTunnel = max(coordinate, tunnelEnd);
            inTunnel = clamp(coordinate - tunnelEnd, -tunnelLength, 0);
            postTunnel = min(0, coordinate - tunnelStart);
            break;
        case 0xC0000000:
            preTunnel = 0;
            if (coordinate > 0) {
                inTunnel = min(tunnelEnd, coordinate);
                postTunnel = max(0, coordinate - tunnelEnd);
            } else {
                inTunnel = max(tunnelStart, coordinate);
                postTunnel = min(0, coordinate - tunnelStart);
            }
            break;
    }
    coordinate = preTunnel + (inTunnel * tunnelMultiplicationFactor) + postTunnel;

    switch (tunnelAxis) {
        case 0x10000000:
            pos.x = coordinate;
            break;
        case 0x20000000:
            pos.y = coordinate;
            break;
        case 0x30000000:
            pos.z = coordinate;
            break;
    }
}
