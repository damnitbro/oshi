/**
 * OSHI (https://github.com/oshi/oshi)
 *
 * Copyright (c) 2010 - 2019 The OSHI Project Team:
 * https://github.com/oshi/oshi/graphs/contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package oshi.hardware.platform.unix.freebsd;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.hardware.Display;
import oshi.hardware.common.AbstractDisplay;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

/**
 * A Display
 */
public class FreeBsdDisplay extends AbstractDisplay {

    private static final Logger LOG = LoggerFactory.getLogger(FreeBsdDisplay.class);

    /**
     * <p>
     * Constructor for FreeBsdDisplay.
     * </p>
     *
     * @param edid
     *            an array of {@link byte} objects.
     */
    public FreeBsdDisplay(byte[] edid) {
        super(edid);
        LOG.debug("Initialized FreeBSDDisplay");
    }

    /**
     * Gets Display Information
     *
     * @return An array of Display objects representing monitors, etc.
     */
    public static Display[] getDisplays() {
        List<String> xrandr = ExecutingCommand.runNative("xrandr --verbose");
        // xrandr reports edid in multiple lines. After seeing a line containing
        // EDID, read subsequent lines of hex until 256 characters are reached
        if (xrandr.isEmpty()) {
            return new Display[0];
        }
        List<Display> displays = new ArrayList<>();
        StringBuilder sb = null;
        for (String s : xrandr) {
            if (s.contains("EDID")) {
                sb = new StringBuilder();
            } else if (sb != null) {
                sb.append(s.trim());
                if (sb.length() < 256) {
                    continue;
                }
                String edidStr = sb.toString();
                LOG.debug("Parsed EDID: {}", edidStr);
                byte[] edid = ParseUtil.hexStringToByteArray(edidStr);
                if (edid.length >= 128) {
                    displays.add(new FreeBsdDisplay(edid));
                }
                sb = null;
            }
        }

        return displays.toArray(new Display[0]);
    }
}
