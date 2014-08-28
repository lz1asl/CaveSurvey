/**
 * @author Alessandro Vernassa <speleoalex@gmail.com>
 * @copyright Copyright (c) 2013
 * @license http://opensource.org/licenses/gpl-license.php GNU General Public License
 */
function getParameterByName(name)
{
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    if (results === null)
        return "";
    else
        return decodeURIComponent(results[1].replace(/\+/g, " "));
}
var lang = getParameterByName("lang");
if (lang === "" || lang === "auto")
{
    try {
        lang = navigator.language.split("-")[0].toLowerCase();
        
    }
    catch (e)
    {
        lang = "en";
    }
}

var config = {
    "lang": lang
};
