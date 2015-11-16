/**
 * @author Alessandro Vernassa <speleoalex@gmail.com>
 * @copyright Copyright (c) 2013
 * @license http://opensource.org/licenses/gpl-license.php GNU General Public License
 */
//------------menu and toolbars-------------------------------------------->

$(".actionopen").click(function() {
    $('#files').click();
    return false;
});
$(".actionadd").click(function() {
    $('#filesadd').click();
    return false;
});

$(".actionsaveas").click(function() {
    ops_SaveAs();
    return false;
});

$(".actioncompile").click(function() {
    ops_compile();
    return false;
});

$(".actionSaveToTherion").click(function() {
    ops_compile();
    ops_SaveToTherion();
    ops_compile();
    return false;
});

$(".actionexportDXFPlan").click(function() {
    ops_compile();
    ops_SaveToDXFPlan(shape);
    ops_compile();
    return false;
});

$(".actionexportDXF").click(function() {
    ops_compile();
    ops_SaveToDXF(shape);
    ops_compile();
    return false;
});
$(".actionSaveToCSV").click(function() {
    ops_SaveToCSV();
    
    return false;
});

$("input").change(function(){
    ops_validateform();
});


$(".actionSaveToDXFSection").click(function() {
    ops_compile();
    ops_SaveToDXFSection(shapeSection);
    ops_compile();
    return false;
});


$(".actionSaveToDXFAll").click(function() {
    ops_compile();
    ops_SaveToDXFAll();
    ops_compile();
    return false;
});

$(".actionSaveToSVGAll").click(function() {
    ops_compile();
    ops_SaveToSVGAll();
    return false;
});


$(".actionSaveToKML").click(function() {
    ops_SaveToKML(shape);
    return false;
});

$(".actionnewproject").click(function() {
    console.log("new project");
    ops_NewFile();
    return false;
});

$(".actionexit").click(function() {
    CaveSurveyJSInterface.goBack();
});

$(".zoom_original").click(function() {

    $(this).parent().children('input.zoom').val(1);
    ops_redrawCanvas();
}

);
$(".zoom_all").click(function() {

    $(this).parent().children('input.zoom').val(-1);
    ops_redrawCanvas();
}

);

$(".zoom_inc").click(function() {

    $(this).parent().children('input.zoom').val(parseFloat($(this).parent().children('input.zoom').val()) + 0.1);
    ops_redrawCanvas();
}

);

$(".zoom_dec").click(function() {
    var zoom = parseFloat($(this).parent().children('input.zoom').val());
    zoom = zoom - 0.1;
    if (zoom > 0)
        $(this).parent().children('input.zoom').val(zoom);
    ops_redrawCanvas();

}

);


$(".x_inc").click(function() {

    $(this).parent().children('input.xrotation').val(parseInt($(this).parent().children('input.xrotation').val()) + 10);
    ops_redrawCanvas();

}

);

$(".x_dec").click(function() {

    $(this).parent().children('input.xrotation').val(parseInt($(this).parent().children('input.xrotation').val()) - 10);
    ops_redrawCanvas();

}

);


$(".y_inc").click(function() {

    $(this).parent().children('input.yrotation').val(parseInt($(this).parent().children('input.yrotation').val()) + 10);
    ops_redrawCanvas();

}

);

$(".y_dec").click(function() {

    $(this).parent().children('input.yrotation').val(parseInt($(this).parent().children('input.yrotation').val()) - 10);
    ops_redrawCanvas();

}

);

$(".z_inc").click(function() {

    $(this).parent().children('input.zrotation').val(parseInt($(this).parent().children('input.zrotation').val()) + 10);
    ops_redrawCanvas();

}

);

$(".z_dec").click(function() {

    $(this).parent().children('input.zrotation').val(parseInt($(this).parent().children('input.zrotation').val()) - 10);
    ops_redrawCanvas();

}

);




$(".move_up").click(function() {

    $(this).parent().children('input.ypan').val(parseInt($(this).parent().children('input.ypan').val()) - 10);
    ops_redrawCanvas();
}

);

$(".move_down").click(function() {

    $(this).parent().children('input.ypan').val(parseInt($(this).parent().children('input.ypan').val()) + 10);
    ops_redrawCanvas();
}

);

$(".move_left").click(function() {

    $(this).parent().children('input.xpan').val(parseInt($(this).parent().children('input.xpan').val()) - 10);
    ops_redrawCanvas();
}

);

$(".move_right").click(function() {

    $(this).parent().children('input.xpan').val(parseInt($(this).parent().children('input.xpan').val()) + 10);
    ops_redrawCanvas();
}

);

$(".prosp").click(function() {

    if (parseInt($(this).parent().children('input.prosp').val()) === 0)
    {
        $(this).css("border", "1px solid #00ff00");
        $(this).parent().children('input.prosp').val(1);
    }
    else
    {

        $(this).css("border", "1px solid red");
        $(this).parent().children('input.prosp').val(0);
    }
    ops_redrawCanvas();
//    ops_compile();

}

);

$(".layer_text").click(function() {

    if (parseInt($(this).parent().children('input.layer_text').val()) === 0)
    {
        $(this).css("border", "1px solid #00ff00");
        $(this).parent().children('input.layer_text').val(1);
    }
    else
    {

        $(this).css("border", "1px solid red");
        $(this).parent().children('input.layer_text').val(0);
    }
    ops_redrawCanvas();
//    ops_compile();

}

);


$("#cave_utm_x").blur(
        function() {
            if (!isNaN($("#cave_utm_zone").val()) && !isNaN($("#cave_utm_x").val()) && !isNaN($("#cave_utm_y").val()) && $("#cave_utm_zone").val() !== "" && $("#cave_utm_x").val() !== "" && $("#cave_utm_y").val() !== "")
            {
                ops_CalcCoords($("#cave_utm_x").val(), $("#cave_utm_y").val(), $("#cave_utm_zone").val());
            }
            ops_validateform();
        }
);
$("#cave_utm_y").blur(
        function() {
            if (!isNaN($("#cave_utm_zone").val()) && !isNaN($("#cave_utm_x").val()) && !isNaN($("#cave_utm_y").val()) && $("#cave_utm_zone").val() !== "" && $("#cave_utm_x").val() !== "" && $("#cave_utm_y").val() !== "")
            {
                ops_CalcCoords($("#cave_utm_x").val(), $("#cave_utm_y").val(), $("#cave_utm_zone").val());
            }
            ops_validateform();
        }
);
$("#cave_utm_zone").blur(
        function() {
            if (!isNaN($("#cave_utm_zone").val()) && !isNaN($("#cave_utm_x").val()) && !isNaN($("#cave_utm_y").val()) && $("#cave_utm_zone").val() !== "" && $("#cave_utm_x").val() !== "" && $("#cave_utm_y").val() !== "")
            {
                ops_CalcCoords($("#cave_utm_x").val(), $("#cave_utm_y").val(), $("#cave_utm_zone").val());
            }
            ops_validateform();
        }
);
$("#cave_longitude").blur(
        function() {
            if (!isNaN($("#cave_longitude").val()) && !isNaN($("#cave_latitude").val()) && $("#cave_latitude").val() !== "" && $("#cave_longitude").val() !== "")
            {
                ops_CalcCoords($("#cave_longitude").val(), $("#cave_latitude").val(), "");
            }
            
            ops_validateform();
        }
);
$("#cave_latitude").blur(
        function() {
            if (!isNaN($("#cave_longitude").val()) && !isNaN($("#cave_latitude").val()) && $("#cave_latitude").val() !== "" && $("#cave_longitude").val() !== "")
            {
                ops_CalcCoords($("#cave_longitude").val(), $("#cave_latitude").val(), "");
            }
            ops_validateform();
        }
);



//------------menu and toolbars--------------------------------------------<    