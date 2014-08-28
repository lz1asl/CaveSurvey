
function ops_validateform()
{
    if (isNaN($("#cave_latitude").val()) || $("#cave_latitude").val() == "")
    {
        $("#cave_latitude").css("border-color", "#ff0000");
    }
    else
    {
        $("#cave_latitude").css("border-color", "#dadada");
    }
    if (isNaN($("#cave_longitude").val()) || $("#cave_longitude").val() === "")
    {
        $("#cave_longitude").css("border-color", "#ff0000");
    }
    else
    {
        $("#cave_longitude").css("border-color", "#dadada");
    }
    if (isNaN($("#cave_utm_x").val()) || $("#cave_utm_x").val() === "")
    {
        $("#cave_utm_x").css("border-color", "#ff0000");
    }
    else
    {
        $("#cave_utm_x").css("border-color", "#dadada");
    }

    if (isNaN($("#cave_utm_y").val()) || $("#cave_utm_y").val() === "")
    {
        $("#cave_utm_y").css("border-color", "#ff0000");
    }
    else
    {
        $("#cave_utm_y").css("border-color", "#dadada");
    }

    if (isNaN($("#cave_utm_zone").val()) || $("#cave_utm_zone").val() === "")
    {
        $("#cave_utm_zone").css("border-color", "#ff0000");
    }
    else
    {
        $("#cave_utm_zone").css("border-color", "#dadada");
    }
    if (isNaN($("#cave_altitude").val()) || $("#cave_altitude").val() === "")
    {
        $("#cave_altitude").css("border-color", "#ff0000");
    }
    else
    {
        $("#cave_altitude").css("border-color", "#dadada");
    }

    if (ops_point_exists($("#cave_geoPoint").val()))
    {
        $("#cave_geoPoint").css("border-color", "#dadada");
    }
    else
    {
        $("#cave_geoPoint").css("border-color", "#ff0000");
    }
    if (ops_point_exists($("#cave_startPoint").val()))
    {
        $("#cave_startPoint").css("border-color", "#dadada");
    }
    else
    {
        $("#cave_startPoint").css("border-color", "#ff0000");
    }

}

function ops_point_exists(point) {
    if (point === "")
        return false;
    for (var i in caveObj.data)
    {
       // console.log(caveObj.data[i]);
        if (caveObj.data[i].from === point)
            return true;
        if (caveObj.data[i].to === point)
            return true;
    }
    return false;
}


