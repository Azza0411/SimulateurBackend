// src/main/java/com/demo/demo/serviceimplement/TestService.java
package com.demo.demo.serviceimplement;

import com.demo.demo.DTO.TestResultDto;
import com.demo.demo.DTO.TestSubmitDto;
import com.demo.demo.entities.TestQuestion;
import com.demo.demo.repository.TestQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestService {

    @Autowired
    private TestQuestionRepository repo;

    public List<TestQuestion> getAllQuestions() {
        List<TestQuestion> all = repo.findByIsActiveTrue();
        Map<String, List<TestQuestion>> byCategory = all.stream()
                .collect(Collectors.groupingBy(TestQuestion::getCategory));

        List<TestQuestion> selected = new ArrayList<>();
        selected.addAll(randomSelectAndShuffle(byCategory.get("TECHNIQUE"), 10));
        selected.addAll(randomSelectAndShuffle(byCategory.get("PSYCHOLOGIQUE"), 8));
        selected.addAll(randomSelectAndShuffle(byCategory.get("EXPERIENCE"), 6));

        return selected;
    }

    private List<TestQuestion> randomSelectAndShuffle(List<TestQuestion> list, int n) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        Collections.shuffle(list);
        return list.stream().limit(n).peek(q -> {
            String[] options = q.getOptions();
            int correctIdx = q.getCorrectAnswerIndex();
            List<String> shuffled = new ArrayList<>(Arrays.asList(options));
            Collections.shuffle(shuffled);
            q.setOption1(shuffled.get(0));
            q.setOption2(shuffled.get(1));
            q.setOption3(shuffled.get(2));
            q.setOption4(shuffled.get(3));
            q.setCorrectAnswer(shuffled.get(correctIdx));
        }).collect(Collectors.toList());
    }

    public TestResultDto submitTest(TestSubmitDto dto) {
        Map<Long, Integer> answers = dto.getAnswers();
        List<TestQuestion> questions = getAllQuestions();

        int techScore = 0, psychoScore = 0, expScore = 0;
        int techTotal = 0, psychoTotal = 0, expTotal = 0;

        for (TestQuestion q : questions) {
            Integer userIdx = answers.get(q.getId());
            int correctIdx = q.getCorrectAnswerIndex();
            boolean correct = userIdx != null && userIdx == correctIdx;

            switch (q.getCategory()) {
                case "TECHNIQUE" -> { if (correct) techScore++; techTotal++; }
                case "PSYCHOLOGIQUE" -> { if (correct) psychoScore++; psychoTotal++; }
                case "EXPERIENCE" -> { if (correct) expScore++; expTotal++; }
            }
        }

        double tsi = calculateTSI(techScore, psychoScore, expScore, techTotal, psychoTotal, expTotal);
        String level = getLevel(tsi);
        String profile = getProfile(techScore, psychoScore, expScore, techTotal, psychoTotal, expTotal);
        String badge = getBadge(tsi);
        String strengths = getStrengths(techScore, psychoScore, expScore, techTotal, psychoTotal, expTotal);
        String weaknesses = getWeaknesses(techScore, psychoScore, expScore, techTotal, psychoTotal, expTotal);

        TestResultDto result = new TestResultDto();
        result.setLevel(level);
        result.setTechnique(techScore);
        result.setPsycho(psychoScore);
        result.setExperience(expScore);
        result.setTsi(tsi);
        result.setProfile(profile);
        result.setBadge(badge);
        result.setStrengths(strengths);
        result.setWeaknesses(weaknesses);
        return result;
    }

    private double calculateTSI(int t, int p, int e, int tt, int pt, int et) {
        double techPct = t * 1.0 / tt;
        double psychoPct = p * 1.0 / pt;
        double expPct = e * 1.0 / et;
        return Math.round((techPct * 0.4 + psychoPct * 0.35 + expPct * 0.25) * 100.0) / 100.0;
    }

    private String getLevel(double tsi) {
        if (tsi >= 0.75) return "Avancé";
        if (tsi >= 0.50) return "Intermédiaire";
        return "Débutant";
    }

    private String getProfile(int t, int p, int e, int tt, int pt, int et) {
        double techPct = t * 1.0 / tt;
        double psychoPct = p * 1.0 / pt;
        double expPct = e * 1.0 / et;

        if (techPct >= 0.8 && psychoPct >= 0.7) return "Trader Analytique";
        if (psychoPct >= 0.8) return "Trader Discipliné";
        if (expPct >= 0.7) return "Trader Expérimenté";
        if (techPct <= 0.4) return "Trader Novice";
        return "Trader Équilibré";
    }

    private String getBadge(double tsi) {
        if (tsi >= 0.90) return "Maître du Marché";
        if (tsi >= 0.75) return "Expert Certifié";
        if (tsi >= 0.50) return "Trader Confirmé";
        return "Apprenti Trader";
    }

    private String getStrengths(int t, int p, int e, int tt, int pt, int et) {
        List<String> s = new ArrayList<>();
        if (t >= 8) s.add("Excellentes connaissances techniques");
        if (p >= 6) s.add("Bonne maîtrise émotionnelle");
        if (e >= 5) s.add("Solide expérience pratique");
        return s.isEmpty() ? "Continuez à apprendre !" : String.join(", ", s);
    }

    private String getWeaknesses(int t, int p, int e, int tt, int pt, int et) {
        List<String> w = new ArrayList<>();
        if (t <= 4) w.add("Renforcer les bases techniques");
        if (p <= 3) w.add("Travailler la discipline émotionnelle");
        if (e <= 2) w.add("Gagner plus d'expérience en live");
        return w.isEmpty() ? "Aucun point faible majeur" : String.join(", ", w);
    }
}