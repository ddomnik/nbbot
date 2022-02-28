package com.company;

import org.ini4j.Wini;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        Wini ini_settings;
        boolean method_nvidia_api = false;
        boolean method_nbb = false;

        try {
            ini_settings = new Wini(new File("settings.ini"));

            method_nvidia_api = ini_settings.get("method", "nvidia_api", boolean.class);
            method_nbb = ini_settings.get("method", "nbb", boolean.class);

            if(!method_nvidia_api && !method_nbb){
                out.println("No checking method selected.");
                out.println("Terminating app.");
                System.exit(0);
            }

            int interval = ini_settings.get("general", "interval", int.class);

            boolean no_match = true;

            //Play startup sound
            playSound();
            
            //Send initial telegram message
            if (ini_settings.containsKey("general")) {
                if (ini_settings.get("general", "use_telegram", boolean.class)) {
                    if (ini_settings.containsKey("telegram")) {
                        String token = ini_settings.get("telegram", "token", String.class);
                        String chat_id = ini_settings.get("telegram", "chat_id", String.class);
                        if (!token.equals("null") && !chat_id.equals("null")) {
                            telegram_notify(token, chat_id, "Bot started and will notify you");
                        } else {
                            out.println("Error: Telegram parameters are not set up.");
                        }
                    }
                }
            }

            while (no_match) {
                out.println("");
                out.println(new Date());
                out.println("");

                if (method_nvidia_api) {
                    if (check_via_nvidia(ini_settings)) {
                        no_match = false;
                    }
                }

                if (method_nbb) {
                    if (check_via_nbb(ini_settings)) {
                        no_match = false;
                    }
                }

                if (no_match) {
                    out.println("");
                    out.println("checking availability in "+interval+" seconds again...");
                    Thread.sleep(interval * 1000);
                } else {
                    out.println("");
                    out.println("███╗   ███╗ █████╗ ████████╗ ██████╗██╗  ██╗");
                    out.println("████╗ ████║██╔══██╗╚══██╔══╝██╔════╝██║  ██║");
                    out.println("██╔████╔██║███████║   ██║   ██║     ███████║");
                    out.println("██║╚██╔╝██║██╔══██║   ██║   ██║     ██╔══██║");
                    out.println("██║ ╚═╝ ██║██║  ██║   ██║   ╚██████╗██║  ██║");
                    out.println("╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝    ╚═════╝╚═╝  ╚═╝");
                    out.println("");
                    out.println("App is inactive. Please close and restart the app to check for prices again.");
                    out.println("");

                    for (int i = 0; i < 5; i++) {
                        playSound();
                        Thread.sleep(1500);
                    }
                }
            }
        }catch (IOException e) {
            out.println("Error reading 'settings.ini'. Please make sure the file is in the same folder as the 'nbbot.jar'.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            out.println("Failed to schedule check for every minute.");
            e.printStackTrace();
            e.printStackTrace();
        }
    }


    public static boolean check_via_nvidia(Wini ini_settings){
        out.println("Using NVIDIA API:");
        out.println("");

        boolean match = false;

        ArrayList<String> check_for = new ArrayList<>();
        ArrayList<String> nvidia_product_names = new ArrayList<>();

        if(ini_settings.containsKey("check"))
        {
            //Search ini for what cards should be looked for
            for(String key_name : ini_settings.get("check").keySet()){
                if(ini_settings.get("check", key_name,boolean.class)){
                    check_for.add(key_name);
                }
            }

            //Create array of cards product names that should be checked for
            if(ini_settings.containsKey("nvidia_product_names")){
                out.println("  checking for:");
                for(int i = 0 ; i < check_for.size() ; i++){
                    String product_name = ini_settings.get("nvidia_product_names", check_for.get(i),String.class);
                    nvidia_product_names.add(product_name);

                    out.println("  - "+check_for.get(i)+" ("+product_name+")");
                }

                out.println("");

                //Retrieve webpage and check for cards
                if(ini_settings.containsKey("paths_nvidia"))
                {
                    //First API call
                    out.println("  using first API link");
                    String url_nvidia_api = ini_settings.get("paths_nvidia","nvidia_api_1",String.class);

                    String result = null;
                    try {
                        result = Jsoup.connect(url_nvidia_api).ignoreContentType(true).get().body().html();
                        JSONObject resultJSON = new JSONObject(result);

                        if(resultJSON.getBoolean("success")){

                            JSONArray items = resultJSON.getJSONArray("listMap");
                            out.println("    found "+items.length()+" items:");
                            for(int i = 0 ; i < items.length() ; i ++){

                                JSONObject itemJSON = items.getJSONObject(i);

                                String str_url = itemJSON.getString("product_url");
                                str_url +="/action/login";
                                String str_name = itemJSON.getString("fe_sku");
                                out.print("    - "+str_name+" ");

                                if(nvidia_product_names.stream().anyMatch(str_name::contains)) {
                                    out.println("");
                                    if(itemJSON.getString("is_active").equals("true")) {
                                        //We have an active buy link
                                        out.println("      "+itemJSON.toString());
                                        webpage_open(str_url);
                                        //Check if telegram is set up
                                        if (ini_settings.containsKey("general")) {
                                            if (ini_settings.get("general", "use_telegram", boolean.class)) {
                                                if (ini_settings.containsKey("telegram")) {
                                                    String token = ini_settings.get("telegram", "token", String.class);
                                                    String chat_id = ini_settings.get("telegram", "chat_id", String.class);
                                                    if (!token.equals("null") && !chat_id.equals("null")) {
                                                        telegram_notify(token, chat_id, str_url);
                                                    } else {
                                                        out.println("Error: Telegram parameters are not set up.");
                                                    }
                                                }
                                            }
                                        }
                                        //We found at least one match
                                        match = true;
                                    }
                                }else{
                                    out.println("(not in checked)"); //The item is not in our checked list
                                }
                            }
                            out.println("");
                        }else{
                            out.println("Result was not defined as success");
                        }
                    } catch (IOException e) {
                        out.println("Connection error.");
                        e.printStackTrace();
                    }


                    //Second API call
                    out.println("  using second API link");
                    url_nvidia_api = ini_settings.get("paths_nvidia","nvidia_api_2",String.class);

                    result = null;
                    try {
                        result = Jsoup.connect(url_nvidia_api).ignoreContentType(true).get().body().html();

                        JSONObject resultJSON = new JSONObject(result);
                        JSONObject searchedJSON = resultJSON.getJSONObject("searchedProducts");

                        JSONObject prod_featuredJSON = searchedJSON.getJSONObject("featuredProduct");
                        JSONArray  items = searchedJSON.getJSONArray("productDetails");

                        items.put(prod_featuredJSON);
                        out.println("    found "+items.length()+" items:");

                        for(int i = 0 ; i < items.length() ; i ++){

                            JSONObject itemJSON = items.getJSONObject(i);

                            String str_url = itemJSON.getJSONArray("retailers").getJSONObject(0).getString("purchaseLink");
                            str_url +="/action/login";
                            String str_name = itemJSON.getString("productSKU");
                            out.print("    - "+str_name+" ");

                            if(nvidia_product_names.stream().anyMatch(str_name::contains)) {
                                out.println("");
                                if(itemJSON.getBoolean("productAvailable")) {
                                    //We have an active buy link
                                    out.println("      "+itemJSON.toString());
                                    webpage_open(str_url);
                                    //Check if telegram is set up
                                    if (ini_settings.containsKey("general")) {
                                        if (ini_settings.get("general", "use_telegram", boolean.class)) {
                                            if (ini_settings.containsKey("telegram")) {
                                                String token = ini_settings.get("telegram", "token", String.class);
                                                String chat_id = ini_settings.get("telegram", "chat_id", String.class);
                                                if (!token.equals("null") && !chat_id.equals("null")) {
                                                    telegram_notify(token, chat_id, str_url);
                                                } else {
                                                    out.println("Error: Telegram parameters are not set up.");
                                                }
                                            }
                                        }
                                    }
                                    //We found at least one match
                                    match = true;
                                }
                            }else{
                                out.println("(not in checked)"); //The item is not in our checked list
                            }
                        }
                        out.println("");
                    } catch (IOException e) {
                        out.println("Connection error.");
                        e.printStackTrace();
                    }
                }
            }else{
                out.println("Could not find section [nvidia_product_names] in 'settings.ini.'");
            }
        }else{
            out.println("Could not find section [check] in 'settings.ini.'");
        }

        if(!match){
            out.println("  ... no active buy link found.");
        }

        return match;

    }

    public static boolean check_via_nbb(Wini ini_settings){
        out.println("NBB METHOD IS NOT IMPLEMENTED YET!");
        return false;


            /*
            boolean nologin = false;

            String url_nvidia_api = "https://api.store.nvidia.com/partner/v1/feinventory?skus=de~NVGFT090~NVGFT080~NVGFT080T~NVGFT070T~NVGFT070~NVGFT060T&locale=de";
            String url_3060   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15614038&sort=price&order=asc&availability=alle";
            String url_3070   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15613065&sort=price&order=asc&availability=alle";
            String url_3070ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15614913&sort=price&order=asc&availability=alle";
            String url_3080   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15613064&sort=price&order=asc&availability=alle";
            String url_3080ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15614857&sort=price&order=asc&availability=alle";
            String url_3090   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15613063&sort=price&order=asc&availability=alle";

            /* Product pages with login popup
            String url_3060   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15614038/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
            String url_3070   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15614913/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
            String url_3070ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15614913/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
            String url_3080   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15613064/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
            String url_3080ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15614857/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
            String url_3090   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15613063/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
             */
            /*
            //Check this type:  3060, 3070, 3070ti, 3080, 3080ti, 3090
            String[] name =     {"3060",   "3070",   "3070ti",   "3080",   "3080ti",   "3090"};
            String[] name_api = {"NVGFT060T_DE",   "NVGFT070_DE",   "NVGFT070T_DE",   "NVGFT080_DE",   "NVGFT080T_DE",   "NVGFT090_DE"};
            Boolean[] check =   {false,    false,    false,      false,    false,      false };
            float[] msrp =      {330,      520,      620,        720,      1200,       1550 };                //float[] msrp =    {330,      520,      620,        720,      1200,       1550 };
            String[] url =      {url_3060, url_3070, url_3070ti, url_3080, url_3080ti, url_3090};
            float msrp_threshold = 20;

            if(args.length > 0){

                for (int i = 0; i < args.length; i++) {

                    System.out.println("Argument " + i + ": " + args[i]);
                    for(int j = 0; j < name.length; j++){
                        if(args[i].equals("-"+name[j])){
                            check[j] = true;
                        }
                    }

                    if(args[i].equals("-nologin")){
                        out.println("");
                        out.println("using no-login urls");
                        nologin = true;
                    }

                    if(args[i].contains("-threshold=")){
                        String threshold_str = args[i].substring("-threshold=".length());
                        out.println("");
                        out.println("using threshold "+threshold_str+" EUR");
                        msrp_threshold = Float.parseFloat(threshold_str);
                    }
                }
            }



                    //Check via NBB Page
                    /*
                    out.println("Via NBB page:");
                    for(int i = 0; i < check.length; i++) {
                        if(check[i])
                        {
                            try {
                                out.println("Checking price for "+name[i]+" with msrp lower or equal to "+(msrp[i]+msrp_threshold)+" EUR.");
                                doc = Jsoup.connect(url[i]).get();
                                Elements items = doc.select(".js-listing-item-GTM");
                                for (Element item : items) {
                                    String price_str = item.attr("data-price");
                                    if(!price_str.isEmpty()){
                                        out.println(" - card found with price: "+item.attr("data-price"));
                                        float price = Float.parseFloat(price_str);
                                        if(price <= msrp[i]+msrp_threshold){
                                            out.println("MATCH FOUND!");
                                            no_match = false;
                                            String item_url = item.child(0).child(0).attr("href");
                                            if(!item_url.isEmpty()){
                                                if(!nologin) {
                                                    item_url += "/action/login";
                                                }

                                                //Send Telegram Message
                                                new TelegramNotifier(URLEncoder.encode(item_url));

                                                Desktop desktop = java.awt.Desktop.getDesktop();
                                                try {
                                                    URI oURL = new URI(item_url);
                                                    desktop.browse(oURL);
                                                } catch (URISyntaxException e) {
                                                    out.println("Error opening webpage");
                                                    e.printStackTrace();
                                                }
                                            }else{
                                                out.println("item url field not found (check for updates / nbb website changes)");
                                            }
                                        }
                                    }
                                    else{
                                        out.println("price field not found (check for updates / nbb website changes)");
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

    */
    }

    public static void telegram_notify(String token, String chat_id, String message){
        new TelegramNotifier(token, chat_id, URLEncoder.encode(message));
    }

    public static void webpage_open(String url){
        Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            URI oURL = new URI(url);
            desktop.browse(oURL);
        } catch (URISyntaxException e) {
            out.println("Error opening webpage");
            e.printStackTrace();
        } catch (IOException e) {
            out.println("Error opening webpage (desktop)");
            e.printStackTrace();
        }
    }

    public static void playSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(Main.class.getResource("notification.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch(Exception ex) {
            System.out.println("Error playing sound.");
            ex.printStackTrace();
        }
    }
}
