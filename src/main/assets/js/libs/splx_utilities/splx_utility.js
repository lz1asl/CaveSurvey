var __incFlag = false;
var __incArr = new Array();

/**
 * 
 * @type type
 */
var splx = new function() {
    __incFlag = false;
    if (window.location.toString().search(/^http/i) === 0)
    {
        this.isonline = true;
    }
    else
    {
        this.isonline = false;
    }
    this.include = function(filename, defer) {
        var isMSIE = /*@cc_on!@*/0;

        if (!isMSIE)
        {
            __incArr[filename] = false;

            if (__incFlag === true)
            {
                setTimeout("splx.include('" + filename + "'," + defer + ");", 10);
                return;
            }
            for (var i in __incArr)
            {
                if (__incArr[i] === false && i !== filename)
                {
                    setTimeout("splx.include('" + filename + "'," + defer + ");", 10);
                    return;
                }
                if (i === filename)
                    break;
            }
            __incFlag = true;
            defer = (typeof(defer) !== 'undefined') ? defer : true;

        }


        var d = document;
        var isXML = d.documentElement.nodeName !== 'HTML' || !d.write; // Latter is for silly comprehensiveness
        var js = d.createElementNS && isXML ? d.createElementNS('http://www.w3.org/1999/xhtml', 'script') : d.createElement('script');
        js.setAttribute('type', 'text/javascript');
        js.setAttribute('src', filename);
        if (defer)
        {
            js.setAttribute('defer', 'defer');
        }
        js.setAttribute('onerror', '__incFlag= false;__incArr["' + filename + '"]=true;alert("' + filename + ' not found");');
        js.setAttribute('onload', '__incFlag = false;__incArr["' + filename + '"]=true');
        //js.setAttribute('onload', '__incFlag = false;__incArr["' + filename + '"]=true;splx.print_r("incluso:' + filename + '");');


        d.getElementsByTagNameNS && isXML ? (d.getElementsByTagNameNS('http://www.w3.org/1999/xhtml', 'head')[0] ? d.getElementsByTagNameNS('http://www.w3.org/1999/xhtml', 'head')[0].appendChild(js) : d.documentElement.insertBefore(js, d.documentElement.firstChild) // in case of XUL
                ) : d.getElementsByTagName('head')[0].appendChild(js);
        // save include state for reference by include_once
        var cur_file = {};
        cur_file[window.location.href] = 1;
        // BEGIN REDUNDANT
        this.php_js = this.php_js || {};
        // END REDUNDANT
        if (!this.php_js.includes) {
            this.php_js.includes = cur_file;
        }
        if (!this.php_js.includes[filename]) {
            this.php_js.includes[filename] = 1;
        } else {
            this.php_js.includes[filename]++;
        }
        return this.php_js.includes[filename];
    };
    this.rand = function(min, max) {
        var argc = arguments.length;
        if (argc === 0) {
            min = 0;
            max = 2147483647;
        } else if (argc === 1) {
            throw new Error('Warning: rand() expects exactly 2 parameters, 1 given');
        }
        return Math.floor(Math.random() * (max - min + 1)) + min;

    };
    this.file_exists = function(url) {
        var req = this.window.ActiveXObject ? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
        if (!req) {
            throw new Error('XMLHttpRequest not supported');
        }
        req.open('HEAD', url, false);
        req.send(null);
        if (req.status === 200) {
            return true;
        }
        return false;
    };
    this.print_r = function(array, return_val) {
        var output = '',
                pad_char = ' ',
                pad_val = 4,
                getFuncName = function(fn) {
            var name = (/\W*function\s+([\w\$]+)\s*\(/).exec(fn);
            if (!name) {
                return '(Anonymous)';
            }
            return name[1];
        },
                repeat_char = function(len, pad_char) {
            var str = '';
            for (var i = 0; i < len; i++) {
                str += pad_char;
            }
            return str;
        },
                formatArray = function(obj, cur_depth, pad_val, pad_char) {
            if (cur_depth > 0) {
                cur_depth++;
            }

            var base_pad = repeat_char(pad_val * cur_depth, pad_char);
            var thick_pad = repeat_char(pad_val * (cur_depth + 1), pad_char);
            var str = '';

            if (typeof obj === 'object' && obj !== null && obj.constructor && getFuncName(obj.constructor) !== 'PHPJS_Resource') {
                str += 'Array\n' + base_pad + '(\n';
                for (var key in obj) {
                    if (Object.prototype.toString.call(obj[key]) === '[object Array]') {
                        str += thick_pad + '[' + key + '] => ' + formatArray(obj[key], cur_depth + 1, pad_val, pad_char);
                    }
                    else {
                        str += thick_pad + '[' + key + '] => ' + obj[key] + '\n';
                    }
                }
                str += base_pad + ')\n';
            }
            else if (obj === null || obj === undefined) {
                str = '';
            }
            else { // for our "resource" class
                str = obj.toString();
            }

            return str;
        };
        output = formatArray(array, 0, pad_val, pad_char);
        if (return_val !== true) {
            console.log(output);
            return true;
        }
        return output;
    };
    this.time = function() {
        return Math.floor(new Date().getTime() / 1000);
    };
};

