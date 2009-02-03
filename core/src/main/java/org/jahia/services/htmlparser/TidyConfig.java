/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.htmlparser;


/**
 * <p>Title: Tidy Config</p>
 *
 * tidy-mark: bool
 *   If set to yes (the default) Tidy will add a meta element to the document head to indicate that the document has been tidied. To suppress this, set tidy-mark to no. Tidy won't add a meta element if one is already present.
 * markup: bool
 *   Determines whether Tidy generates a pretty printed version of the markup. Bool values are either yes or no. Note that Tidy won't generate a pretty printed version if it finds unknown tags, or missing trailing quotes on attribute values, or missing trailing '>' on tags. The default is yes.
 * wrap: number
 *   Sets the right margin for line wrapping. Tidy tries to wrap lines so that they do not exceed this length. The default is 66. Set wrap to zero if you want to disable line wrapping.
 * wrap-attributes: bool
 *   If set to yes, attribute values may be wrapped across lines for easier editing. The default is no. This option can be set independently of wrap-scriptlets
 * wrap-script-literals: bool
 *   If set to yes, this allows lines to be wrapped within string literals that appear in script attributes. The default is no. The example shows how Tidy wraps a really really long script string literal inserting a backslash character before the linebreak:
 *   <a href="somewhere.html" onmouseover="document.status = '...some \
 *   really, really, really, really, really, really, really, really, \
 *   really, really long string..';">test</a>
 * wrap-asp: bool
 *   If set to no, this prevents lines from being wrapped within ASP pseudo elements, which look like: <%� ...� %>. The default is yes.
 * wrap-jste: bool
 *   If set to no, this prevents lines from being wrapped within JSTE pseudo elements, which look like: <#� ...� #>. The default is yes.
 * wrap-php: bool
 *   If set to no, this prevents lines from being wrapped within PHP pseudo elements. The default is yes.
 * literal-attributes: bool
 *   If set to yes, this ensures that whitespace characters within attribute values are passed through unchanged. The default is no.
 * tab-size: number
 *   Sets the number of columns between successive tab stops. The default is 4. It is used to map tabs to spaces when reading files. Tidy never outputs files with tabs.
 * indent: no, yes or auto
 *   If set to yes, Tidy will indent block-level tags. The default is no. If set to auto Tidy will decide whether or not to indent the content of tags such as title, h1-h6, li, td, th, or p depending on whether or not the content includes a block-level element. You are advised to avoid setting indent to yes as this can expose layout bugs in some browsers.
 * indent-spaces: number
 *   Sets the number of spaces to indent content when indentation is enabled. The default is 2 spaces.
 * indent-attributes: bool
 *   If set to yes, each attribute will begin on a new line. The default is no.
 * hide-endtags: bool
 *   If set to yes, optional end-tags will be omitted when generating the pretty printed markup. This option is ignored if you are outputting to XML. The default is no.
 * input-xml: bool
 *   If set to yes, Tidy will use the XML parser rather than the error correcting HTML parser. The default is no.
 * output-xml: bool
 *   If set to yes, Tidy will generate the pretty printed output writing it as well-formed XML. Any entities not defined in XML 1.0 will be written as numeric entities to allow them to be parsed by an XML parser. The tags and attributes will be in the case used in the input document, regardless of other options. The default is no.
 * add-xml-pi: bool
 * add-xml-decl: bool
 *   If set to yes, Tidy will add the XML declatation when outputting XML or XHTML. The default is no. Note that if the input document includes an <?xml?> declaration then it will appear in the output independent of the value of this option.
 * output-xhtml: bool
 *   If set to yes, Tidy will generate the pretty printed output writing it as extensible HTML. The default is no. This option causes Tidy to set the doctype and default namespace as appropriate to XHTML. If a doctype or namespace is given they will checked for consistency with the content of the document. In the case of an inconsistency, the corrected values will appear in the output. For XHTML, entities can be written as named or numeric entities according to the value of the "numeric-entities" property. The tags and attributes will be output in the case used in the input document, regardless of other options.
 * doctype: omit, auto, strict, loose or <fpi>
 *   This property controls the doctype declaration generated by Tidy. If set to omit the output file won't contain a doctype declaration. If set to auto (the default) Tidy will use an educated guess based upon the contents of the document. If set to strict, Tidy will set the doctype to the strict DTD. If set to loose, the doctype is set to the loose (transitional) DTD. Alternatively, you can supply a string for the formal public identifier (fpi) for example:
 *   doctype: "-//ACME//DTD HTML 3.14159//EN"
 *   If you specify the fpi for an XHTML document, Tidy will set the system identifier to the empty string. Tidy leaves the document type for generic XML documents unchanged.
 * char-encoding: raw, ascii, latin1, utf8 or iso2022
 *   Determines how Tidy interprets character streams. For ascii, Tidy will accept Latin-1 character values, but will use entities for all characters whose value > 127. For raw, Tidy will output values above 127 without translating them into entities. For latin1 characters above 255 will be written as entities. For utf8, Tidy assumes that both input and output is encoded as UTF-8. You can use iso2022 for files encoded using the ISO2022 family of encodings e.g. ISO 2022-JP. The default is ascii.
 * numeric-entities: bool
 *   Causes entities other than the basic XML 1.0 named entities to be written in the numeric rather than the named entity form. The default is no
 * quote-marks: bool
 *   If set to yes, this causes " characters to be written out as &quot; as is preferred by some editing environments. The apostrophe character ' is written out as &#39; since many web browsers don't yet support &apos;. The default is no.
 * quote-nbsp: bool
 *   If set to yes, this causes non-breaking space characters to be written out as entities, rather than as the Unicode character value 160 (decimal). The default is yes.
 * quote-ampersand: bool
 *   If set to yes, this causes unadorned & characters to be written out as &amp;. The default is yes.
 * assume-xml-procins: bool
 *   If set to yes, this changes the parsing of processing instructions to require ?> as the terminator rather than >. The default is no. This option is automatically set if the input is in XML.
 * fix-backslash: bool
 *   If set to yes, this causes backslash characters "\" in URLs to be replaced by forward slashes "/". The default is yes.
 * break-before-br: bool
 *   If set to yes, Tidy will output a line break before each <br> element. The default is no.
 * uppercase-tags: bool
 *   Causes tag names to be output in upper case. The default is no resulting in lowercase, except for XML input where the original case is preserved.
 * uppercase-attributes: bool
 *   If set to yes attribute names are output in upper case. The default is no resulting in lowercase, except for XML where the original case is preserved.
 * word-2000: bool
 *   If set to yes, Tidy will go to great pains to strip out all the surplus stuff Microsoft Word 2000 inserts when you save Word documents as "Web pages". The default is no. Note that Tidy doesn't yet know what to do with VML markup from Word, but in future I hope to be able to map VML to SVG.
 *   Microsoft has developed its own optional filter for exporting to HTML, and the 2.0 version is much improved. You can download the filter free from the Microsoft Office Update site.
 * clean: bool
 *   If set to yes, causes Tidy to strip out surplus presentational tags and attributes replacing them by style rules and structural markup as appropriate. It works well on the html saved from Microsoft Office'97. The default is no.
 * logical-emphasis: bool
 *   If set to yes, causes Tidy to replace any occurrence of i by em and any occurrence of b by strong. In both cases, the attributes are preserved unchanged. The default is no. This option can now be set independently of the clean and drop-font-tags options.
 * drop-empty-paras: bool
 *   If set to yes, empty paragraphs will be discarded. If set to no, empty paragraphs are replaced by a pair of br elements as HTML4 precludes empty paragraphs. The default is yes.
 * drop-font-tags: bool
 *   If set to yes together with the clean option (see above), Tidy will discard font and center tags rather than creating the corresponding style rules. The default is no.
 * enclose-text: bool
 *   If set to yes, this causes Tidy to enclose any text it finds in the body element within a p element. This is useful when you want to take an existing html file and use it with a style sheet. Any text at the body level will screw up the margins, but wrap the text within a p element and all is well! The default is no.
 * enclose-block-text: bool
 *   If set to yes, this causes Tidy to insert a p element to enclose any text it finds in any element that allows mixed content for HTML transitional but not HTML strict. The default is no.
 * fix-bad-comments: bool
 *   If set to yes, this causes Tidy to replace unexpected hyphens with "=" characters when it comes across adjacent hyphens. The default is yes. This option is provided for users of Cold Fusion which uses the comment syntax: <!---� --->
 * add-xml-space: bool
 *   If set to yes, this causes Tidy to add xml:space="preserve" to elements such as pre, style and script when generating XML. This is needed if the whitespace in such elements is to be parsed appropriately without having access to the DTD. The default is no.
 * alt-text: string
 *   This allows you to set the default alt text for img attributes. This feature is dangerous as it suppresses further accessibility warnings. YOU ARE RESPONSIBLE FOR MAKING YOUR DOCUMENTS ACCESSIBLE TO PEOPLE WHO CAN'T SEE THE IMAGES!!!
 * write-back: bool
 *   If set to yes, Tidy will write back the tidied markup to the same file it read from. The default is no. You are advised to keep copies of important files before tidying them as on rare occasions the result may not always be what you expect.
 * keep-time: bool
 *   If set to yes, Tidy won't alter the last modified time for files it writes back to. The default is yes. This allows you to tidy files without effecting which ones will be uploaded to the Web server when using a tool such as 'SiteCopy'. Note that this feature may not work on some platforms.
 * error-file: filename
 *   Writes errors and warnings to the named file rather than to stderr.
 * show-warnings: bool
 *   If set to no, warnings are suppressed. This can be useful when a few errors are hidden in a flurry of warnings. The default is yes.
 * quiet: bool
 *   If set to yes, Tidy won't output the welcome message or the summary of the numbers of errors and warnings. The default is no.
 * gnu-emacs: bool
 *   If set to yes, Tidy changes the format for reporting errors and warnings to a format that is more easily parsed by GNU Emacs. The default is no.
 * split: bool
 *   If set to yes Tidy will use the input file to create a sequence of slides, splitting the markup prior to each successive <h2>. You can see an example of the results in a recent talk I made on XHTML. The slides are written to "slide1.html", "slide2.html" etc. The default is no.
 * new-empty-tags: tag1, tag2, tag3
 *   Use this to declare new empty inline tags. The option takes a space or comma separated list of tag names. Unless you declare new tags, Tidy will refuse to generate a tidied file if the input includes previously unknown tags. Remember to also declare empty tags as either inline or blocklevel, see below.
 * new-inline-tags: tag1, tag2, tag3
 *   Use this to declare new non-empty inline tags. The option takes a space or comma separated list of tag names. Unless you declare new tags, Tidy will refuse to generate a tidied file if the input includes previously unknown tags.
 * new-blocklevel-tags: tag1, tag2, tag3
 *   Use this to declare new block-level tags. The option takes a space or comma separated list of tag names. Unless you declare new tags, Tidy will refuse to generate a tidied file if the input includes previously unknown tags. Note you can't change the content model for elements such as table, ul, ol and dl. This is explained in more detail in the release notes.
 * new-pre-tags: tag1, tag2, tag3
 *   Use this to declare new tags that are to be processed in exactly the same way as HTML's pre element. The option takes a space or comma separated list of tag names. Unless you declare new tags, Tidy will refuse to generate a tidied file if the input includes previously unknown tags. Note you can't as yet add new CDATA elements (similar to script).
 *
 *
 *
 *
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */

public class TidyConfig {

    public static final String CHAR_ENCODING = "char-encoding";
    public static final String NEW_INLINE_TAGS = "new-inline-tags";
    public static final String NEW_EMTY_TAGS = "new-empty-tags";
    public static final String NEW_BLOCK_LEVEL_TAGS = "new-blocklevel-tags";
    public static final String OUTPUT_XHTML = "output-xhtml";

}