/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ITerminalSymbols.java 22626 2009-09-30 17:37:31Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.compiler;

/**
 * Maps each terminal symbol in the java-grammar into a unique integer.
 * This integer is used to represent the terminal when computing a parsing action.
 *
 * @see IScanner
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITerminalSymbols {

	int TokenNameWHITESPACE = 1000;
	int TokenNameCOMMENT_LINE = 1001;
	int TokenNameCOMMENT_BLOCK = 1002;
	int TokenNameCOMMENT_JAVADOC = 1003;

	int TokenNameIdentifier = 5;
	int TokenNameabstract = 98;

    /**
     * "assert" token (added in J2SE 1.4).
     */
	int TokenNameassert = 118;
	int TokenNameboolean = 18;
	int TokenNamebreak = 119;
	int TokenNamebyte = 19;
	int TokenNamecase = 211;
	int TokenNamecatch = 225;
	int TokenNamechar = 20;
	int TokenNameclass = 165;
	int TokenNamecontinue = 120;
	int TokenNamedefault = 212;
	int TokenNamedo = 121;
	int TokenNamedouble = 21;
	int TokenNameelse = 213;
	int TokenNameextends = 243;
	int TokenNamefalse = 37;
	int TokenNamefinal = 99;
	int TokenNamefinally = 226;
	int TokenNamefloat = 22;
	int TokenNamefor = 122;
	int TokenNameif = 123;
	int TokenNameimplements = 268;
	int TokenNameimport = 191;
	int TokenNameinstanceof = 65;
	int TokenNameint = 23;
	int TokenNameinterface = 180;
	int TokenNamelong = 24;
	int TokenNamenative = 100;
	int TokenNamenew = 32;
	int TokenNamenull = 38;
	int TokenNamepackage = 214;
	int TokenNameprivate = 101;
	int TokenNameprotected = 102;
	int TokenNamepublic = 103;
	int TokenNamereturn = 124;
	int TokenNameshort = 25;
	int TokenNamestatic = 94;
	int TokenNamestrictfp = 104;
	int TokenNamesuper = 33;
	int TokenNameswitch = 125;
	int TokenNamesynchronized = 85;
	int TokenNamethis = 34;
	int TokenNamethrow = 126;
	int TokenNamethrows = 227;
	int TokenNametransient = 105;
	int TokenNametrue = 39;
	int TokenNametry = 127;
	int TokenNamevoid = 26;
	int TokenNamevolatile = 106;
	int TokenNamewhile = 117;
	int TokenNameIntegerLiteral = 40;
	int TokenNameLongLiteral = 41;
	int TokenNameFloatingPointLiteral = 42;
	int TokenNameDoubleLiteral = 43;
	int TokenNameCharacterLiteral = 44;
	int TokenNameStringLiteral = 45;
	int TokenNamePLUS_PLUS = 1;
	int TokenNameMINUS_MINUS = 2;
	int TokenNameEQUAL_EQUAL = 35;
	int TokenNameLESS_EQUAL = 66;
	int TokenNameGREATER_EQUAL = 67;
	int TokenNameNOT_EQUAL = 36;
	int TokenNameLEFT_SHIFT = 14;
	int TokenNameRIGHT_SHIFT = 11;
	int TokenNameUNSIGNED_RIGHT_SHIFT = 12;
	int TokenNamePLUS_EQUAL = 168;
	int TokenNameMINUS_EQUAL = 169;
	int TokenNameMULTIPLY_EQUAL = 170;
	int TokenNameDIVIDE_EQUAL = 171;
	int TokenNameAND_EQUAL = 172;
	int TokenNameOR_EQUAL = 173;
	int TokenNameXOR_EQUAL = 174;
	int TokenNameREMAINDER_EQUAL = 175;
	int TokenNameLEFT_SHIFT_EQUAL = 176;
	int TokenNameRIGHT_SHIFT_EQUAL = 177;
	int TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 178;
	int TokenNameOR_OR = 80;
	int TokenNameAND_AND = 79;
	int TokenNamePLUS = 3;
	int TokenNameMINUS = 4;
	int TokenNameNOT = 71;
	int TokenNameREMAINDER = 9;
	int TokenNameXOR = 63;
	int TokenNameAND = 62;
	int TokenNameMULTIPLY = 8;
	int TokenNameOR = 70;
	int TokenNameTWIDDLE = 72;
	int TokenNameDIVIDE = 10;
	int TokenNameGREATER = 68;
	int TokenNameLESS = 69;
	int TokenNameLPAREN = 7;
	int TokenNameRPAREN = 86;
	int TokenNameLBRACE = 110;
	int TokenNameRBRACE = 95;
	int TokenNameLBRACKET = 15;
	int TokenNameRBRACKET = 166;
	int TokenNameSEMICOLON = 64;
	int TokenNameQUESTION = 81;
	int TokenNameCOLON = 154;
	int TokenNameCOMMA = 90;
	int TokenNameDOT = 6;
	int TokenNameEQUAL = 167;
	int TokenNameEOF = 158;
	int TokenNameERROR = 309;

    /**
     * "enum" keyword (added in J2SE 1.5).
     * @since 3.0
     */
	int TokenNameenum = 400;

    /**
     * "@" token (added in J2SE 1.5).
     * @since 3.0
     */
	int TokenNameAT = 401;

    /**
     * "..." token (added in J2SE 1.5).
     * @since 3.0
     */
	int TokenNameELLIPSIS = 402;

	/**
	 * @since 3.1
	 */
	int TokenNameconst = 403;

	/**
	 * @since 3.1
	 */
	int TokenNamegoto = 404,

//{ObjectTeams: mirrored from auto-generated TerminalTokens.java. Note: the values here must be constant across Eclipse-versions, so do not simply copy the identifiers!
	// Take values from 5000 upwards
    TokenNamebase = 5000,
    TokenNametsuper = 5001,
    TokenNamecallin = 5002,
    TokenNamewithin = 5003,
    TokenNameplayedBy = 5004,
    TokenNamewith = 5005,
    TokenNameteam = 5006,
    TokenNameas = 5007,
    TokenNameresult = 5008,
    TokenNamereplace = 5009,
    TokenNameafter = 5010,
    TokenNamebefore = 5011,
    TokenNameBINDIN = 5012,
    TokenNameBINDOUT = 5013,
    TokenNameCALLOUT_OVERRIDE = 5014,
	TokenNamereadonly = 5015,
	TokenNameget = 5016,
	TokenNameset = 5017,
	TokenNamewhen = 5018,
    TokenNameprecedence = 5019;
// carp}
}