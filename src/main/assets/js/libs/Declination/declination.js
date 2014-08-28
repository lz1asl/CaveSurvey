Torig = 2003.5;
An1 = 2015;
An0 = An1 - 12;
A = new Array(3);
B = new Array(3);
C = new Array(3);
b = new Array(3);
c = new Array(3);
al = new Array(3);
be = new Array(3);
ga = new Array(3);

// Coefficients for update 2012                 
A[0] = 0.2515327E+00;
B[0] = -0.4084916E-03;
C[0] = 0.3730549E-02;
al[0] = 0.9345965E-01;
b[0] = -0.9064871E-03;
c[0] = -0.5953362E-04;
be[0] = 0.4185884E-01;
ga[0] = 0.4146905E-05;
A[1] = 0.6284161E+02;
B[1] = 0.8359118E-02;
C[1] = 0.7641182E-03;
al[1] = 0.2486779E-03;
b[1] = 0.6226476E-02;
c[1] = 0.5408233E-02;
be[1] = 0.4396101E-01;
ga[1] = 0.0000000E+00;
A[2] = 0.4729032E+05;
B[2] = 0.2921479E+01;
C[2] = 0.6395759E+00;
al[2] = 0.5446762E+02;
b[2] = -0.1889142E-03;
c[2] = 0.9127565E-05;
be[2] = 0.2617642E+01;
ga[2] = 0.4612142E+01;

Ano = new Array();
for (i = 0; i < 4; i++)
    Ano[i] = new Array(4);

X = 0;
Y = 0;
jour = new Date();
Epoch = 2013.1;
Time = Epoch - Torig;
Ad = 0;
Elem = new Array(3);
VTf = new Array(3);
DtR = Math.PI / 180;
xs = -138;
xn = 102;
yw = -124;
ye = 244;
fx = 4;
fy = 4;
ii = 2;
ki = 4;
ia = 1;
jj = 1;
kj = 8;
ja = -1;
cx = 2;
cy = 0;


anomf = "b" + (10 * ii + jj) + ".htm";
Ano[ii][jj] = open(anomf, "Anomaly", "innerHeight=80,innerWidth=100,location,dependent");
Ano[ii][jj].blur();

function f(t, i) {
    return((t + be[i] * t * t) / (1 + ga[i] * t))
}
function calc() {
    for (i = 0; i < 3; i++) {
        Tf = f(Time, i);
        wg = al[i] * (1 + b[i] * X + c[i] * Y);
        Elem[i] = A[i] + B[i] * X + C[i] * Y + wg * Tf;
        DTf = f(Time + 0.5, i) - f(Time - 0.5, i);
        VTf[i] = wg * DTf;
    }
    Elz = Elem[2] * Math.sin(DtR * Elem[1]);
    Elh = Elem[2] * Math.cos(DtR * Elem[1]);
    Elx = Elh * Math.cos(DtR * Elem[0]);
    Ely = Elh * Math.sin(DtR * Elem[0]);
    D = Elem[0] + Ad;
    Sig = "East";
    if (D < 0) {
        Sig = "West";
        D = -D;
    }
    Dd = Math.floor(D);
    Dm = Math.floor(600 * (D - Dd)) / 10;
    I = Elem[1];
    Id = Math.floor(I);
    Im = Math.round(600 * (I - Id)) / 10;

    Mu = (0.57607 * Y + 9.65E-05 * X * Y + 23E-09 * X * X * Y - 8E-09 * Y * Y * Y) / 60;
    Dk = Elem[0] + Ad - Mu;
    Sigk = "East";
    if (Dk < 0) {
        Sigk = "West";
        Dk = -Dk;
    }
    Dkd = Math.floor(Dk);
    Dkm = Math.floor(600 * (Dk - Dkd)) / 10;
    Dkg = Dk * 400 / 360;
    Dkgd = Dkg;
    //Dkgd  = Math.floor (Dkg);
    //Dkgm = Math.floor(1000 * (Dkg - Dkgd))/10;
    Dka = Dk * 6400 / 360;

    document.xyForm.Traite.value = Epoch;
    document.xyForm.resuldd.value = Dd;
    document.xyForm.resuldm.value = Dm;
    document.xyForm.resulds.value = Sig;
    document.xyForm.resuldkd.value = Dkd;
    document.xyForm.resuldkm.value = Dkm;
    document.xyForm.resuldks.value = Sigk;
    document.xyForm.resuldkgd.value = Dkgd;
    //document.xyForm.resuldkgm.value = Dkgm;
    document.xyForm.resuldkgs.value = Sigk;
    document.xyForm.resuldka.value = Dka;
    document.xyForm.resuldkas.value = Sigk;
    document.xyForm.resulid.value = Id;
    document.xyForm.resulim.value = Im;
    document.xyForm.resulf.value = Math.round(Elem[2]);
    document.xyForm.resulvd.value = Math.round(600 * VTf[0]) / 10;
    document.xyForm.resulvi.value = Math.round(600 * VTf[1]) / 10;
    document.xyForm.resulvf.value = Math.round(10 * VTf[2]) / 10;
    document.xyForm.resulx.value = Math.round(Elx);
    document.xyForm.resuly.value = Math.round(Ely);
    document.xyForm.resulz.value = Math.round(Elz);
    document.xyForm.resulh.value = Math.round(Elh);

}
onError = errGest;
function errGest() {
    alert("Data error!");
    return true;
}
function traiterx() {
    X = eval(document.xyForm.Traitx.value) / 1000 + 0.0001;
    if (X < 200 + xs || X >= 200 + xn) {
        alert("Out of range!");
        document.xyForm.Traitx.value = "";
        document.xyForm.Traitx.focus();
    } else {
        X -= 200;
        c1 = (X - xs) / (15 * fx);
        c2 = Math.floor(c1);
        ii = c2;
        c3 = 15 * (c1 - c2);
        ki = Math.round(c3);
        cx = (c3 - ki) * fx;
        ia = ki - c3 >= 0 ? -1 : 1;
        anomf = "b" + (10 * ii + jj) + ".htm";
        Ano[ii][jj] = open(anomf, "Anomaly", "innerHeight=80,innerWidth=100,location,dependent");
        Ano[ii][jj].blur();
        document.xyForm.Traite.focus();
        calc();
    }
}
function traitery() {
    Y = eval(document.xyForm.Traity.value) / 1000 + 0.0001;
    if (Y < 600 + yw || Y >= 600 + ye) {
        alert("Out of range!");
        document.xyForm.Traity.value = "";
        document.xyForm.Traity.focus();
    }
    else {
        Y -= 600;
        c1 = (Y - yw) / (23 * fy);
        c2 = Math.floor(c1);
        jj = c2;
        c3 = 23. * (c1 - c2);
        kj = Math.round(c3);
        cy = (c3 - kj) * fy;
        ja = kj - c3 >= 0 ? -1 : 1;
        anomf = "b" + (10 * ii + jj) + ".htm";
        Ano[ii][jj] = open(anomf, "Anomaly", "innerHeight=80,innerWidth=100,location,dependent");
        Ano[ii][jj].blur();
        document.xyForm.Traite.focus();
    }
}
function traitere() {
    Time = eval(document.xyForm.Traite.value)
    Epoch = Time;
    if (Time < An0 || Time > An1) {
        alert("Valid epoch between " + An0 + " and " + An1);
        document.xyForm.Traite.value = "";
        document.xyForm.Traite.focus();
    }
    else {
        Time -= Torig;
    }
}
function getanol() {
    Ad = 0;
    if (document.xyForm.checkr.checked) {
        if (document.xyForm.checkb.checked) {
            at = (Ano[ii][jj].V[ki + ia][kj] - Ano[ii][jj].V[ki][kj]) / (ia * fx);
            bt = (Ano[ii][jj].V[ki][kj + ja] - Ano[ii][jj].V[ki][kj]) / (ja * fy);
            ct = Ano[ii][jj].V[ki][kj];
            Ad = (at * cx + bt * cy + ct) / 60;
        }
        calc();
        document.xyForm.checkr.checked = !document.xyForm.checkr.checked;
    }
}


