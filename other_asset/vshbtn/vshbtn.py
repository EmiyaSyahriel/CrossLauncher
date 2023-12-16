import fontforge

fnt = fontforge.open("vshbtn.svg")
fnt.addLookup("ligatures", "gsub_ligature", (), [["rlig", [
    ["DFLT", ["dflt"]],
    ["latn", ["dflt"]]
    ]]])
fnt.addLookupSubtable("ligatures", "ligatureshi")

ligatures = [
# PlayStations
    ("PlayStation.Circle",      ":ps_circle:"),
    ("PlayStation.Triangle",    ":ps_triangle:"),
    ("PlayStation.Square",      ":ps_square:"),
    ("PlayStation.Cross",       ":ps_cross:"),
    ("PlayStation.DPadUp",      ":ps_pad_u:"),
    ("PlayStation.DPadDown",    ":ps_pad_d:"),
    ("PlayStation.DPadLeft",    ":ps_pad_l:"),
    ("PlayStation.DPadRight",   ":ps_pad_r:"),
    ("PlayStation.L1",          ":ps_l1:"),
    ("PlayStation.L2",          ":ps_l2:"),
    ("PlayStation.L3",          ":ps_l3:"),
    ("PlayStation.R1",          ":ps_r1:"),
    ("PlayStation.R2",          ":ps_r2:"),
    ("PlayStation.R3",          ":ps_r3:"),
    ("PlayStation.Select",      ":ps_select:"),
    ("PlayStation.Start",       ":ps_start:"),
    ("PlayStation.PS",          ":ps_home:"),
# Xbox One
    ("Xbox.B",                  ":xbox_b:"),
    ("Xbox.A",                  ":xbox_a:"),
    ("Xbox.X",                  ":xbox_x:"),
    ("Xbox.Y",                  ":xbox_y:"),
    ("Xbox.DPadU",              ":xbox_pad_u:"),
    ("Xbox.DPadD",              ":xbox_pad_d:"),
    ("Xbox.DPadL",              ":xbox_pad_l:"),
    ("Xbox.DPadR",              ":xbox_pad_r:"),
    ("Xbox.LT",                 ":xbox_lt:"),
    ("Xbox.LB",                 ":xbox_lb:"),
    ("Xbox.LS",                 ":xbox_ls:"),
    ("Xbox.RT",                 ":xbox_rt:"),
    ("Xbox.RB",                 ":xbox_rb:"),
    ("Xbox.RS",                 ":xbox_rs:"),
    ("Xbox.View",               ":xbox_xbox:"),
    ("Xbox.Menu",               ":xbox_menu:"),
    ("Xbox.Xbox",               ":xbox_home:"),
# Nintendo Switch
    ("Switch.B",                ":switch_b:"),
    ("Switch.A",                ":switch_a:"),
    ("Switch.X",                ":switch_x:"),
    ("Switch.Y",                ":switch_y:"),
    ("Switch.DPadUp",           ":switch_pad_u:"),
    ("Switch.DPadDown",         ":switch_pad_d:"),
    ("Switch.DPadLeft",         ":switch_pad_l:"),
    ("Switch.DPadRight",        ":switch_pad_r:"),
    ("Switch.L",                ":switch_l:"),
    ("Switch.ZL",               ":switch_zl:"),
    ("Switch.LS",               ":switch_ls:"),
    ("Switch.R",                ":switch_r:"),
    ("Switch.ZR",               ":switch_zr:"),
    ("Switch.RS",               ":switch_rs:"),
    ("Switch.Minus",            ":switch_minus:"),
    ("Switch.Plus",             ":switch_plus:"),
    ("Switch.Home",             ":switch_home:"),
# Android
    ("Android.OK",                 ":android_ok:"),
    ("Android.Back",               ":android_back:"),
    ("Android.Delete",             ":android_delete:"),
    ("Android.Menu",               ":android_menu:"),
    ("Android.DPadU",              ":android_pad_u:"),
    ("Android.DPadD",              ":android_pad_d:"),
    ("Android.DPadL",              ":android_pad_l:"),
    ("Android.DPadR",              ":android_pad_r:"),
    ("Android.F1",                 ":android_f1:"),
    ("Android.F2",                 ":android_f2:"),
    ("Android.F3",                 ":android_f3:"),
    ("Android.F4",                 ":android_f4:"),
    ("Android.F5",                 ":android_f5:"),
    ("Android.F6",                 ":android_f6:"),
    ("Android.F11",                ":android_f11:"),
    ("Android.F12",                ":android_f12:"),
    ("Android.Home",               ":android_home:")
    ]

for lig in ligatures:
    (name, ligg) = lig
    ligtup = tuple(map(str, ligg.split()))
    g = fnt[name]
    g.addPosSub("ligatureshi", ligtup)

fnt.generate("vshbtn.otf")
