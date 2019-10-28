import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class testPageNr {

    private static final String allCompaniesUrl = "https://www.allabolag.se/";


    public static void main(String[] args) throws IOException {
        ArrayList<String> nameList = new ArrayList<>();
        File file = new File("out.txt");
        FileWriter fw = new FileWriter(file);
        PrintWriter pw = new PrintWriter(fw);

        pw.println("hej");
        pw.close();

        final String urlsök =
                allCompaniesUrl + "lista/aktiebolag/24/xv/RESEBYR%C3%85%20&%20TURISM";

        final Document document = Jsoup.connect(urlsök).get();
        for (Element row : document.select("div.search-results.container.page h1")) {
            final String nr =
                    row.select("h1.search-results__header:nth-of-type(1)").text();

			int sida = parseResultAndReturnPage(nr);

            final String urlsökAb =
                    urlsök + "/page/" + sida;

            try {
                final Document document2 = Jsoup.connect(urlsökAb).get();
                for (Element row2 : document2.select("div.box-results article,dl")) {
                    final String name =
                            row2.select("article.box").text();
                    if (name.contains("AB")) {
                        nameList.add(name);
                    } else if (name.contains("Aktiebolag")) {
                        nameList.add(name);
                    } else if (name.contains("Handelsbolag")) {
                        nameList.add(name);
                    }
                }

                for (int i = 0; i < nameList.size(); i++) {
                    String str = nameList.get(i);
                    String[] allParts = parseCompanyFindPhoneFindRevenueFindContact(str);
                    for (int a = 0; a < allParts.length - 2; a++) {
                        System.out.println(allParts[a]);
                        fileWriter(allParts[a]);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }


        }

    }

    public static int parseResultAndReturnPage(String nr) {
		String sökresultat[] = nr.split("Sökresultat: ");
		String sökResultatNr = sökresultat[1];
		String resultatNr[] = sökResultatNr.split("träffar");
		String sidor = resultatNr[0].replace(" ", "");
		int pageNr = Integer.parseInt(sidor);
		int sidNr = pageNr / 20;
		Random rand = new Random();
		int sida = rand.nextInt(sidNr);
		System.out.println(nr);
		return sida;
	}


    public static String[] parseCompanyFindPhoneFindRevenueFindContact(String allInfo) {

        String[] returnArr = parseCompanyInfo(allInfo);
        String orgNr = returnArr[4];
        String companyName = returnArr[0];

        final String url1 =
                allCompaniesUrl + orgNr + "/" + formatName(companyName);
        try {
            final Document document = Jsoup.connect(url1).get();
            for (Element row : document.select("div.page.company.container.hidden div")) {
                final String telNr =
                        row.select("div:nth-of-type(2)").text();
                System.out.println("-------------------------------------------------------------------------------------------");
                fileWriter("---------------------------------------------------------------------------------------------");
                System.out.println(url1);
                fileWriter(url1);


                if (telNr.contains("Telefon")) containsTelephone(telNr);
                else System.out.println("tel nr ej tillgängligt");

                if (telNr.contains("Omsättning")) containsRevenue(telNr);
                else System.out.println("omsättning ej tillgänglig");


                final String url =
                        allCompaniesUrl + orgNr + "/" + formatName(companyName);
                try {
                    findContactPerson(url);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.out.print("");
                }
                return returnArr;
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return null;
    }

    public static String[] parseCompanyInfo(String allInfo) {
        List<String> splitted = Arrays.asList(allInfo.split(" "));
        int orgNrIndex = splitted.indexOf("Org.nummer");
        String companyName = "";
        for (int i = 0; i < orgNrIndex - 1; i++) {
            String ending = i == orgNrIndex - 2 ? "" : "-";
            companyName += splitted.get(i) + ending;
        }
        String location = splitted.get(orgNrIndex - 1);
        String orgNrOriginal = splitted.get(orgNrIndex + 1);
        String[] orgNrParts = splitted.get(orgNrIndex + 1).split("-");
        String orgNr = orgNrParts[0] + orgNrParts[1];

        String companyDescription = "";
        for (int i = orgNrIndex + 3; i < splitted.size(); i++) {
            companyDescription += splitted.get(i);
        }
        String urlName = companyName;
        return new String[]{companyName, location, orgNrOriginal, companyDescription, orgNr, urlName};
    }


    public static void containsTelephone(String input) throws IOException {
        String telefon[] = input.split("Telefon ");
        String telefonNr = telefon[1];
        String telTel[] = telefonNr.split(" ");
        System.out.println("tel nr: " + telTel[0]);
        fileWriter(telTel[0]);
    }

    public static void containsRevenue(String input) throws IOException {
        String omsättning[] = input.split("Omsättning ");
        String omsättningsNr = omsättning[1];
        String Omsättning[] = omsättningsNr.split("Res");
        System.out.println("omsättning: " + Omsättning[0] + "tkr");
        fileWriter(Omsättning[0] + "tKr");
    }

    public static void findContactPerson(String inputUrl) throws IOException {
        final Document document1 = Jsoup.connect(inputUrl).get();
        for (Element row1 : document1.select("div.cc-flex-grid a")) {
            final String name =
                    row1.select("a.btn-link").text();
            String kontaktPerson[] = name.split("Läs mer");
            String Person = kontaktPerson[0];
            System.out.println(Person);
            fileWriter(Person);
        }
    }


    public static String formatName(String oldName) {
        return oldName.replaceAll("å", "a").replaceAll("ä", "a").replaceAll("ö", "o").replaceAll("&", "").replaceAll("--", "-");
    }

    public static void fileWriter(String output) throws IOException {
        File file = new File("tjena.txt");
        FileWriter fw = new FileWriter(file, true);
        PrintWriter pw = new PrintWriter(fw);

        for (int count = 0; count < 1; count++) {
            pw.println(output);

        }
        pw.close();

    }

}