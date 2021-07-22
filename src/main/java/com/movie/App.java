package com.movie;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
        App ap = new App();
        String dirPath = System.getProperty("user.dir");
        Path sourceTitle = Paths.get(dirPath + "/title.tsv.gz");
        Path targetTitle = Paths.get(dirPath + "/title.tsv");
        Path sourceRating = Paths.get(dirPath + "/ratings.tsv.gz");
        Path targetRating = Paths.get(dirPath + "/ratings.tsv");
        Path resultPath = Paths.get(dirPath + "/result.tsv");
        try {
            if (Files.notExists(sourceTitle)) {
                ap.imdbDownloadFile("https://datasets.imdbws.com/title.basics.tsv.gz", "title.tsv.gz");

                ap.decompressGzipNio(sourceTitle, targetTitle);
            }
            if (Files.notExists(sourceRating)) {
                ap.imdbDownloadFile("https://datasets.imdbws.com/title.ratings.tsv.gz", "ratings.tsv.gz");

                ap.decompressGzipNio(sourceRating, targetRating);
            }
            ap.readTitle(targetTitle.toString(), targetRating.toString(), resultPath.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void imdbDownloadFile(String sturl, String fileName) throws Exception {
        URL url = new URL(sturl);
        try (InputStream in = url.openStream()) {
            Files.copy(in, Paths.get(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decompressGzipNio(Path source, Path target) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(source.toFile()))) {

            Files.copy(gis, target);
        }

    }

    public String[] readTitle(String path, String ratingpath, String resultPath) {
        FileInputStream inputStream = null;
        Scanner sc = null;

        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");
            // int rand=(int)(Math.random() * 1000000);
            // skipLines(sc, rand);
            int count = 0;
            int cnt = 0;
            while (sc.hasNextLine()) {

                String line = sc.nextLine();
                if (count != 0) {
                    String[] parseLine = line.split("\t");
                    String[] tittle = readRatings(ratingpath, parseLine);
                    System.out.println("mergeArr " + Arrays.toString(parseLine) + " " + Arrays.toString(tittle));

                    if (parseLine != null && tittle != null) {
                        int ratingLen = parseLine.length; // determines length of firstArray
                        int tittleLen = tittle.length;
                        String[] mergeArr = new String[ratingLen + tittleLen];

                        System.arraycopy(parseLine, 0, mergeArr, 0, ratingLen);
                        System.arraycopy(tittle, 0, mergeArr, ratingLen, tittleLen);
                        String result = String.join("\t", mergeArr)+"\n";

                        resultFileWrite(resultPath, result);

                        // return parseLine;
                    }
                }
                count++;
                cnt++;

                // System.out.println(line);

            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (sc != null) {
                    sc.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String[] readRatings(String path, String[] titleLine) {

        FileInputStream inputStream = null;
        Scanner sc = null;

        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");

            int cnt = 0;
            while (sc.hasNextLine()) {

                String line = sc.nextLine();

                String[] parseLine = line.split("\t");
                System.out.println("readRatings parseLine " + parseLine[0] + " " + titleLine[0]);

                if (parseLine[0].equals(titleLine[0])) {
                    return parseLine;
                }

                cnt++;
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (sc != null) {
                    sc.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void skipLines(Scanner s, int lineNum) {
        for (int i = 0; i < lineNum; i++) {
            if (s.hasNextLine())
                s.nextLine();
        }
    }

    public void resultFileWrite(String fileName, String contentToAppend) throws IOException {

        Files.write(Paths.get(fileName), contentToAppend.getBytes(), StandardOpenOption.APPEND);
    }

}
