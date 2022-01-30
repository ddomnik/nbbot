package com.company;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;

import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {

        boolean nologin = false;

        String url_3060   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15614038&sort=price&order=asc&availability=alle";
        String url_3060ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15613592&sort=price&order=asc&availability=alle";
        String url_3070   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15613065&sort=price&order=asc&availability=alle";
        String url_3070ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15614913&sort=price&order=asc&availability=alle";
        String url_3080   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15613064&sort=price&order=asc&availability=alle";
        String url_3080ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15614857&sort=price&order=asc&availability=alle";
        String url_3090   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/page/1?box_8308_2816%5B%5D=15613063&sort=price&order=asc&availability=alle";

        /* Product pages with login popup
        String url_3060   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15614038/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
        String url_3060ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15613592/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
        String url_3070   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15613065/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
        String url_3070ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15614913/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
        String url_3080   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15613064/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
        String url_3080ti = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15614857/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
        String url_3090   = "https://www.notebooksbilliger.de/pc+hardware/grafikkarten/action/login/box_8308_2816%255B0%255D/15613063/sort/price/order/asc/availability/alle/categories_id/2816/page/1";
         */

        //Check this type:  3060, 3060ti, 3070, 3070ti, 3080, 3080ti, 3090
        String[] name =   {"3060",   "3060ti",   "3070",   "3070ti",   "3080",   "3080ti",   "3090"};
        Boolean[] check = {false,    false,      false,    false,      false,    false,      false };
        float[] msrp =    {330,      400,        520,      620,        720,      1200,       1550 };                //float[] msrp =    {330,      520,      620,        720,      1200,       1550 };
        String[] url =    {url_3060, url_3060,   url_3070, url_3070ti, url_3080, url_3080ti, url_3090};
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

        //Check if for particular card is checked - if not: check all
        boolean filter = false;
        if(Arrays.asList(check).contains(true)){
            filter = true;
        }

        if(!filter){
            Arrays.fill(check,true);
        }


        playSound();


        Document doc = null;
        boolean no_match = true;

        try {
            while(no_match){
                out.println("");
                out.println(new Date());
                out.println("");

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

                if(no_match){
                    out.println("");
                    out.println("checking prices in a minute again...");
                    Thread.sleep(60 * 1000);
                }else{
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

                    for(int i = 0 ; i < 5 ; i++){
                        playSound();
                        Thread.sleep( 1500);
                    }
                }
            }
        } catch (InterruptedException e) {
            out.println("Failed to schedule check for every minute.");
            e.printStackTrace();
        }

    }

    public static void playSound() {
        try {


            //File file = new File(Main.class.getResource("notification.wav").getFile());
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(Main.class.getResource("notification.wav"));

            //AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("notification.wav")); //working if .wav is in root folder (nbbot)

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch(Exception ex) {
            System.out.println("Error playing sound.");
            ex.printStackTrace();
        }
    }
}
