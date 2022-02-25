### _nbbot_ ist ein Bot, um RTX Grafikkarten bei _notebooksbilliger.de_ zum UVP (Normalpreis) zu ergattern.

_notebooksbilliger.de_ hat die exklusive Partnerschaft, die NVIDIA 3000 Reihe zum UVP anbieten zu können. 
Leider sind diese sehr schnell ausverkauft und kommen nur unregelmäßig wieder in die Regale.

Da die aktuellen Preise maßlos überzogen und unmoralisch sind, habe ich diese kleine App geschrieben, die jedem die Möglichkeit geben soll, eine Grafikkarte zu ergattern.
Die App prüft im Minutentakt die Webseite von _notebooksbilliger.de_ nach Grafikkarten zum UVP. Sollte eine Karte erhältich sein, so wird im Standardbrowser die Seite geöffnet, damit man die Grafikkarte mit nur wenigen klicks im Warenkorb hat.
Es lohnt sich deshalb, die App nebenher laufen zu lassen.

__Quickstart:__
1. Klicke rechts auf [releases](https://github.com/ddomnik/nbbot/releases/tag/v1.0.0) und lade die _v1.0.0.rar_ herunter und entpacke sie an gewünschtem Ort. Alternativ kannst du die Dateien [_nbbot.jar_](nbbot_jar/) und [_settings.ini_](nbbot_jar/) direkt im Ordner _nbbot_jar_ herunter.
1. Öffne die Befehlszeile (cmd) im selben Ordner der _nbbot.jar_. Alternativ kann mit einem Doppelklick auf _nbbot.jar_ die Anwendung im Hintergrund gestartet werden.
    - Unter Windows: Shift+Rechtsklick -> "PowerShell-Fenster hier öffnen.
    <img src="img_powershell.jpg" width="600" height="350">
1. Gib den Befehl `java -jar nbbot.jar` ein um das Programm zu starten. Es ertönt ein akustisches Signal. 
1. Fertig! Im Fenster werden nun alle Informationen angezeigt.
    <img src="img_app.jpg" width="600" height="450">  
1. Sobald eine Karte zum UVP gefunden wurde, wird automatisch die Seite der Karte geöffnet mit Login Popup.
    <img src="img_nbb.jpg" width="600" height="400">  
    
1. Einstellungen können in der _settings.ini_ vorgenommen werden. Die Datei kann mit einem standard Texteditor geöffnet werden.
    - unter [check] kann mit einem Boolean (true/false) ausgewählt werden, welche Karten überprüft werden sollen.


__Telegram Benachrichtigung:__

- Erstelle einen Telegram Bot, indem du die Telegram App öffnest (zum Aufsetzen wird die Webapp empfohlen, da so leichter die Werte kopiert werden können)
- Gib im Suchfeld `@BotFather` ein und starte den Chat
- Erstelle einen Bot indem du im Chat `/newbot` eingibst
- Gib dem Bot einen beliebigen Namen. Z.B. `RTX Bot`
- Danach kannst du dem Bot einen beliebigen Usernamen geben. Dieser muss mit `_bot` enden. Z.B. `rtx_bot`
- Nun solltest du eine Nachricht mit einem Link zum Bot (z.B. t.me/rtx_bot) und einen Token bekommen haben. Der Token sieht z.B. so aus: `1234567890:AAkfuwefkdhjesfrsdgo_rzehsefo43ef33`
- Klicke auf den Link zu deinem Bot und starte ihn.
- Schicke eine beliebige Nachricht.
- Öffne nun im Browser folgende URL: `https://api.telegram.org/botTOKEN/getUpdates`, wobei TOKEN durch deinen Token ersetzt werden muss. Die Seite sollte nun etwa folgender Inhalt zeigen:
  
```
{
   "ok":true,
   "result":[
      {
         "update_id":11111111,
         "message":{
            "message_id":1,
            "from":{
               "id":9999999999,
               "is_bot":false,
               "first_name":"WHATEVER",
               "username":"WHATEVER",
               "language_code":"en"
            },
            "chat":{
               "id":9999999999,
               "first_name":"WHATEVER",
               "username":"WHATEVER",
               "type":"private"
            },
            "date":1645717481,
            "text":"Test"
         }
      }
   ]
}
```

- Setze nun in der _settings.ini_ unter [general] den Wert _use_telegram_ = true 
- Unter [telegram] muss der _token_ und die _chat_id_ gesetzt werden.
- _token_ ist der Token aus der Telegram Nachricht (`1234567890:AAkfuwefkdhjesfrsdgo_rzehsefo43ef33`). Die _chat_id_ ist der Wert unter chat -> id (`9999999999`)
  

      
__TIPPs:__
Einen Account im vorhinein anlegen und Kredikarteninformationen hinterlegen! Grafikkarten sind nur per Kreditkarte oder Amazon Pay erhältlich.
    

__DISCLAIMER:__
Das Projekt steht weder mit _NVIDIA_, noch mit _notebooksbilliger.de_ in Verbindung! Jegliche Benutzung der App ist auf eigenem Risiko abzuwägen. Es wird keine Haftung übernommen! 
