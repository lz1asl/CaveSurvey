/**
 * @author Alessandro Vernassa <speleoalex@gmail.com>
 * @copyright Copyright (c) 2013
 * @license http://opensource.org/licenses/gpl-license.php GNU General Public License
 */

/**
 * 
 * @param {type} str
 * @param {type} LowerCaseMode
 * @param {type} lang
 */
function _i18n(str, LowerCaseMode, lang)
{
    try {
        var l = "en";
        var strori = str;
        if (config !== undefined && config.lang !== undefined)
        {
            l = config.lang;
        }
        LowerCaseMode = (typeof (LowerCaseMode) !== 'undefined') ? LowerCaseMode : "";
        if (LowerCaseMode === "auto")
        {
            str = str.toLowerCase();
        }
        lang = (typeof (lang) !== 'undefined') ? lang : l;
        var ret = "";
        if (typeof (__language) === 'undefined' || __language[lang] === undefined || __language[lang][str] === undefined)
        {
            ret = str;
        }
        else
        {
            ret = __language[lang][str];
        }

        if (LowerCaseMode === "auto")
        {


            if (strori[0].search(/[A-Z]/) !== -1 && strori[1].search(/[a-z]/) !== -1)
            {
                LowerCaseMode = "Aa";
            }
            else if (strori[0].search(/[A-Z]/) !== -1 && strori[1].search(/[A-Z]/) !== -1)
            {
                LowerCaseMode = "AA";
            }
            else if (strori[0].search(/[a-z]/) !== -1 && strori[1].search(/[a-z]/) !== -1)
            {
                LowerCaseMode = "aa";
            }
        }
        //------------------------------------------------------------------------->
        switch (LowerCaseMode)
        {
            case "Aa":
                ret = ret.charAt(0).toUpperCase() + ret.slice(1);
                break;
            case "AA":
                ret = ret.toUpperCase();
                break;
            case "aa":
                ret = ret.toLowerCase();
                break;
        }
        //-------------------------------------------------------------------------<
        return ret;
    }
    catch (e) {
        return str;
    }
}


/**
 * 
 * @returns undefined
 */
function TranslateHtml()
{
    $("[data-i18n='Aa']").each(function() {
        var contents = $(this).contents();
        if (contents.length > 0) {
            if (contents.get(0).nodeType === Node.TEXT_NODE) {
                $(this).html(_i18n($(this).html(), "Aa")).append(contents.slice(1));
            }
        }
    });
    $("[data-i18n='AA']").each(function() {
        var contents = $(this).contents();
        if (contents.length > 0) {
            if (contents.get(0).nodeType === Node.TEXT_NODE) {
                $(this).html(_i18n($(this).html(), "AA")).append(contents.slice(1));
            }
        }
    });
    $("[data-i18n='aa']").each(function() {
        var contents = $(this).contents();
        if (contents.length > 0) {
            if (contents.get(0).nodeType === Node.TEXT_NODE) {
                $(this).html(_i18n($(this).html(), "aa")).append(contents.slice(1));
            }
        }
    });

    $("[data-i18n='']").each(function() {
        var contents = $(this).contents();
        if (contents.length > 0) {
            if (contents.get(0).nodeType === Node.TEXT_NODE) {
                $(this).html(_i18n($(this).html())).append(contents.slice(1));
            }
        }
    });
    $("[data-i18n='auto']").each(function() {
        var contents = $(this).contents();
        if (contents.length > 0) {
            if (contents.get(0).nodeType === Node.TEXT_NODE) {
                $(this).html(_i18n($(this).html(), "auto")).append(contents.slice(1));
            }
        }
    });
    $("[data-i18n-title]").each(function() {
        var contents = $(this).contents();
        $(this).attr("title", _i18n($(this).attr("data-i18n-title"), "auto"));
    });

}