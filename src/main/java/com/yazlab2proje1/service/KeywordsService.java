    package com.yazlab2proje1.service;

    import java.io.File;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import org.apache.pdfbox.pdmodel.PDDocument;
    import org.apache.pdfbox.text.PDFTextStripper;
    import org.springframework.stereotype.Service;

    @Service
    public class KeywordsService {

        List<String> keywords = new ArrayList<>();

        public List<String> findKeywords(File file) {

            try {
                
                keywords.clear();
                
                PDDocument document = PDDocument.load(file);
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String text = pdfStripper.getText(document);
                document.close();

                Map<String, List<String>> keywordMap = getKeywordMap();

                for (String category : keywordMap.keySet()) {
                    for (String keyword : keywordMap.get(category)) {
                        int count = countOccurrences(text.toLowerCase(), keyword.toLowerCase());
                        if (count >= 5) //Belki değiştirilebilir 
                        { 
                            keywords.add(category);
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            
            

            return keywords;
        }

        public  int countOccurrences(String text, String keyword) {
            int count = 0;
            int index = text.indexOf(keyword);
            while (index != -1) {
                count++;
                index = text.indexOf(keyword, index + 1);
            }
            return count;
        }

        private Map<String, List<String>> getKeywordMap() {
            Map<String, List<String>> keywordMap = new HashMap<>();

            keywordMap.put("Derin Öğrenme", Arrays.asList("deep learning", "neural network", "CNN", "RNN", "LSTM"));
            keywordMap.put("Doğal Dil İşleme", Arrays.asList("NLP", "sentiment analysis", "tokenization"));
            keywordMap.put("Bilgisayarla Görü", Arrays.asList("computer vision", "image processing", "OpenCV"));
            keywordMap.put("Generatif Yapay Zeka", Arrays.asList("generative ai", "GAN", "diffusion model", "text-to-image"));
            keywordMap.put("Beyin-Bilgisayar Arayüzleri (BCI)", Arrays.asList("EEG", "brain waves", "neuroscience", "brain signals"));
            keywordMap.put("Kullanıcı Deneyimi Tasarımı", Arrays.asList("UX", "UI", "usability", "interaction design"));
            keywordMap.put("Artırılmış ve Sanal Gerçeklik (AR/VR)", Arrays.asList("virtual reality", "augmented reality", "immersive experience"));
            keywordMap.put("Veri Madenciliği", Arrays.asList("data mining", "pattern recognition", "clustering", "classification", "association rules"));
            keywordMap.put("Veri Görselleştirme", Arrays.asList("data visualization", "matplotlib"));
            keywordMap.put("Veri İşleme Sistemleri", Arrays.asList("big data", "Spark", "MapReduce", "distributed computing"));
            keywordMap.put("Zaman Serisi Analizi", Arrays.asList("time series", "temporal data", "LSTM", "RNN", "trend analysis"));

            return keywordMap;
        }

    }
