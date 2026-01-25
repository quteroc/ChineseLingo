Konspekt projektu

APLIKACJA DO NAUKI CHIŃSKIEGO

Autorzy:

* Yauheni Butsialevich  
* Karolina Strzelecka

	

## **1\. Opis i Cel**

Celem jest stworzenie aplikacji w Javie, która pomaga uczyć się chińskich znaków w inteligentnej kolejności. Zamiast kuć na pamięć losowe słowa, program podpowiada znaki, które są podobne do tych, które użytkownik już zna (np. mają ten sam element graficzny) i są często używane w języku.

**Główna idea:** "Znasz znak A? To naucz się znaku B, bo zawiera części które już znasz i jest popularny."

## **2\. Jak to działa (Bez Bazy Danych)**

Aplikacja jest w pełni samodzielna. Nie wymaga instalowania niczego poza samą Javą.

1. **Start:** Aplikacja wczytuje pliki tekstowe (słownik i teksty chińskie) do pamięci RAM.  
2. **Działanie:** Wszystkie operacje dzieją się w pamięci komputera (szybko i prosto).  
3. **Zapis:** Postępy użytkownika (lista znaków, które już zna) mogą być zapisane do małego pliku JSON (np. `postepy.json`) na dysku.

   ## **3\. Wykorzystane dane (Pliki)**

Aplikacja korzysta z 3 gotowych plików tekstowych:

1. **Struktura (HanziPy/IDS):** Żeby wiedzieć, że znak "Drzewo" jest częścią znaku "Las".  
2. **Popularność (SUBTLEX):** Żeby nie uczyć rzadkich znaków na początku.  
3. **Słownik (CC-CEDICT):** Żeby wiedzieć, co znak znaczy.

   ## **4\. Wymagania Funkcjonalne (Co robi program)**

1. **Wczytywanie danych:**  
   * Przy uruchomieniu program ładuje pliki do grafu (biblioteka JGraphT).  
2. **Nauka (Rekomendacja):**  
   * Użytkownik klika "Chcę nowy znak".  
   * Program szuka w grafie "sąsiadów" znaków, które użytkownik już zna.  
   * Program wybiera ten, który jest najczęściej używany (wg SUBTLEX) \- może być wybrany według rozkładu na podstawie częstości pojawiania się.  
3. **Oznaczanie postępu:**  
   * Użytkownik może kliknąć "Już to umiem". Znak trafia do listy znanych w postaci grafu lub listy.  
4. **Słownik:**  
   * Wyświetlanie tłumaczenia i pinyinu dla wylosowanego znaku.

   ## **5\. Technologie**

* **Język:** Java.  
* **Biblioteka Grafowa:** JGraphT (do łączenia znaków w sieć).  
* **Interfejs:** Prosta strona w przeglądarce (Spring Boot) LUB zwykła aplikacja okienkowa (JavaFX/Swing) \- do wyboru.  
* **Dane:** Zwykłe pliki `.txt` i `.json`.

## **6\. Wymagania Funkcjonalne (WF)**

### **Grupa: Inicjalizacja i Dane**

* **WF-01:** Aplikacja podczas startu musi wczytać pliki źródłowe (IDS, SUBTLEX, CC-CEDICT) do pamięci operacyjnej.  
* **WF-02:** System musi zweryfikować spójność plików danych (np. czy znaki w grafie strukturalnym mają swoje odpowiedniki w słowniku).  
* **WF-03:** System musi obsługiwać zapis i odczyt pliku profilu użytkownika (`savegame`), zawierającego zbiór nauczonych ID.

### **Grupa: Logika i Algorytmy (In-Memory)**

* **WF-04:** Algorytm rekomendacji musi operować bezpośrednio na obiekcie grafu w pamięci (Java Heap), wyszukując sąsiadów węzła i filtrując ich przez `HashSet` znanych znaków.  
* **WF-05:** Wyszukiwanie w słowniku musi odbywać się poprzez szybkie struktury typu `HashMap` (czas dostępu O(1)), bez zapytań SQL.

### **Grupa: Interfejs**

* (Bez zmian) – Wizualizacja, oznaczanie jako "Umiem", Nauka pisania.

---

## **7\. Wymagania Niefunkcjonalne (WNF)**

### **Technologiczne**

* **WNF-01:** Język implementacji: **Java**.  
* **WNF-02:** Wykorzystanie biblioteki **Jackson** lub **Gson** do serializacji stanu użytkownika do pliku JSON.  
* **WNF-03:** Brak wymogu instalacji zewnętrznego silnika bazy danych (NoSQL/SQL).

### **Wydajnościowe**

* **WNF-04:** Czas uruchomienia aplikacji (tzw. "Cold Start" \- parsowanie plików i budowa grafu) nie może przekraczać 5-10 sekund na standardowym komputerze.  
* **WNF-05:** Zużycie pamięci RAM przez aplikację nie powinno przekraczać 500 MB (biorąc pod uwagę rozmiar słowników).

---

## **8. Uwagi Dotyczące Wyświetlania (Windows)**

Jeśli używasz terminala Windows (PowerShell) i zamiast chińskich znaków widzisz znaki zapytania (`?`) lub prostokąty, wykonaj poniższe kroki:

### **1. Ustawienie Kodowania UTF-8**
Przed uruchomieniem aplikacji w PowerShell wpisz:
```powershell
chcp 65001
$env:JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
```

### **2. Zmiana Czcionki w Terminalu**
Upewnij się, że Twój terminal używa czcionki obsługującej znaki chińskie:
1. Wejdź w **Ustawienia** VS Code (`Ctrl+,`).
2. Wyszukaj `Terminal › Integrated: Font Family`.
3. Dodaj `'MS Gothic'` lub `'SimSun'` do listy czcionek (np. `'Cascadia Code', 'MS Gothic', monospace`).

