/*******************************************************************************
 * Copyright (c) 2011 neXtep Software and contributors.
 * All rights reserved.
 *
 * This file is part of neXtep designer.
 *
 * NeXtep designer is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 *
 * NeXtep designer is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.mysql.services.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.nextep.designer.dbgm.mysql.services.IMySqlModelService;

/**
 * @author Christophe Fondacci
 */
public class MySqlModelService implements IMySqlModelService {

	private final List<String> charsets = Arrays.asList("big5", "dec8", "cp850", "hp8", "koi8r", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"latin1", "latin2", "swe7", "ascii", "ujis", "sjis", "hebrew", "tis620", "euckr", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			"koi8u", "gb2312", "greek", "cp1250", "gbk", "latin5", "armscii8", "utf8", "ucs2", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			"cp866", "keybcs2", "macce", "macroman", "cp852", "latin7", "cp1251", "cp1256", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"cp1257", "binary", "geostd8", "cp932", "eucjpms", "utf8mb4"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	private final List<String> charsetNames = Arrays
			.asList("big5 - Big5 Traditional Chinese", //$NON-NLS-1$
					"dec8 - DEC West European", "cp850 - DOS West European", "hp8 - HP West European", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"koi8r - KOI8-R Relcom Russian", "latin1 - cp1252 West European", //$NON-NLS-1$ //$NON-NLS-2$
					"latin2 - ISO 8859-2 Central European", "swe7 - 7bit Swedish", "ascii - US ASCII", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"ujis - EUC-JP Japanese", "sjis - Shift-JIS Japanese", "hebrew - ISO 8859-8 Hebrew", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"tis620 - TIS620 Thai", "euckr - EUC-KR Korean", "koi8u - KOI8-U Ukrainian", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"gb2312 - GB2312 Simplified Chinese", "greek - ISO 8859-7 Greek", //$NON-NLS-1$ //$NON-NLS-2$
					"cp1250 - Windows Central European", "gbk - GBK Simplified Chinese", //$NON-NLS-1$ //$NON-NLS-2$
					"latin5 - ISO 8859-9 Turkish", "armscii8 - ARMSCII-8 Armenian", "utf8 - UTF-8 Unicode", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"ucs2 - UCS-2 Unicode", "cp866 - DOS Russian", "keybcs2 - DOS Kamenicky Czech-Slovak", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"macce - Mac Central European", "macroman - Mac West European", //$NON-NLS-1$ //$NON-NLS-2$
					"cp852 - DOS Central European", "latin7 - ISO 8859-13 Baltic", //$NON-NLS-1$ //$NON-NLS-2$
					"cp1251 - Windows Cyrillic", "cp1256 - Windows Arabic", "cp1257 - Windows Baltic", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"binary - Binary pseudo charset", "geostd8 - GEOSTD8 Georgian", //$NON-NLS-1$ //$NON-NLS-2$
					"cp932 - SJIS for Windows Japanese", "eucjpms - UJIS for Windows Japanese", "utf8mb4 - 4-Byte UTF-8 Unicode"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private final List<String> charsetDefaultCollations = Arrays.asList("big5_chinese_ci", //$NON-NLS-1$
			"dec8_swedish_ci", "cp850_general_ci", "hp8_english_ci", "koi8r_general_ci", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"latin1_swedish_ci", "latin2_general_ci", "swe7_swedish_ci", "ascii_general_ci", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"ujis_japanese_ci", "sjis_japanese_ci", "hebrew_general_ci", "tis620_thai_ci", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"euckr_korean_ci", "koi8u_general_ci", "gb2312_chinese_ci", "greek_general_ci", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"cp1250_general_ci", "gbk_chinese_ci", "latin5_turkish_ci", "armscii8_general_ci", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"utf8_general_ci", "ucs2_general_ci", "cp866_general_ci", "keybcs2_general_ci", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"macce_general_ci", "macroman_general_ci", "cp852_general_ci", "latin7_general_ci", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"cp1251_general_ci", "cp1256_general_ci", "cp1257_general_ci", "binary", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"geostd8_general_ci", "cp932_japanese_ci", "eucjpms_japanese_ci", "utf8mb4_unicode_ci"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private final List<String> engines = Arrays.asList(
			"InnoDB", "MyISAM", "MEMORY", "MERGE", "BDB", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"FEDERATED", "ARCHIVE", "CSV", "BLACKHOLE"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	public MySqlModelService() {
		Collections.sort(charsets);
		Collections.sort(charsetNames);
		Collections.sort(charsetDefaultCollations);
	}

	@Override
	public List<String> getCharsetsList() {
		return charsets;
	}

	@Override
	public List<String> getCharsetNamesList() {
		return charsetNames;
	}

	@Override
	public String getCharsetFromName(String charsetName) {
		final int charsetIndex = charsetNames.indexOf(charsetName);
		if (charsetIndex != -1 && charsetIndex < charsets.size()) {
			return charsets.get(charsetIndex);
		}
		return null;
	}

	@Override
	public String getCharsetName(String charset) {
		final int charsetIndex = charsets.indexOf(charset);
		if (charsetIndex != -1 && charsetIndex < charsets.size()) {
			return charsetNames.get(charsetIndex);
		}
		return null;
	}

	@Override
	public List<String> getCollationsList() {
		return charsetDefaultCollations;
	}

	@Override
	public String getDefaultCollation(String charset) {
		final int charsetIndex = charsets.indexOf(charset);
		if (charsetIndex != -1 && charsetIndex < charsetDefaultCollations.size()) {
			return charsetDefaultCollations.get(charsetIndex);
		}
		return null;
	}

	@Override
	public String getDefaultEngine() {
		// TODO Plug preferences-based default
		return "InnoDB"; //$NON-NLS-1$
	}

	@Override
	public List<String> getEngineList() {
		return engines;
	}

}
