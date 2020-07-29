Project Title: **Automatic Generation of Academic Citation Graph (Java Version)**

Source Code and Data: [Wolfram Language version][2], [Python version][3] and Java version

Overview: This tool is written by Java and based on JavaFX. The tool will automatically generate a citation graph of a given set of papers. This is a Java version of similar tools that I previously created in [Python][3] and [Wolfram Language][2].

---------------------------
**3rd party jar files used in this project:**

- Bib Parser: [jbibtex-1.0.17.jar][8]
- Web scrapping: [/selenium-java-3.141.59/\*][9]
- Graph Drawing: jgraphx-4.0.5.jar (i.e. JGraphX of [JGraphT][10])

---------------------------
**Main Folder Structure:**

    /citation_graph  
    ├──/src
    |    └──/application
    |         ├──Main.java    
    |         └──*.java
    ├──/bin
    |    ├──/application
    |    |    └──*.class    
    |    ├──manifest.txt    
    |    └──executable.jar    
    ├──/chrome_driver
    |    └──chromedriver_win32.exe    
    └──/jar_3rd_party              
    |    ├──/selenium-java-XX.YY.ZZ
    |    |    └──*.jar        
    |    ├──jbibtex-XX.YY.ZZ.jar
    |    └──jgraphx-XX.YY.ZZ.jar    
    ├──/sample_input
    |    └──*.bib    
    └──README.md

--------------------------

**Procedures:**

1. Download Chrome Driver at [here][6] with respect to your Chrome version and OS (the current versions stored in `/chrome_driver` is for Chrome version v83, and is for Windows only)
2. Select papers from your references management software (e.g. Mendeley) and export to .bib file.
3. Locate your JavaFX lib path, e.g. `C:\Program Files\Java\javafx-sdk-11.0.2\lib` in Windows 10
4. In your command windows (e.g. `cmd`), execute `java --module-path "C:\Program Files\Java\javafx-sdk-11.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar executable.jar` to run `executable.jar`.
5. If you modified the source code and would like to generate a new jar file, go inside `/bin` and run `jar -cfm executable.jar manifest.txt .`
---------------------------

**Trouble shooting:**

1. If your Chrome driver doesn't work, please confirm you are using the correct driver version.
2. Some references quoted by the paper may not be contained in the database (i.e. [Astrophysics Data System of Harvard][4]). If the number of references of a specific paper exceed 25 in the database, and only the first 25 references will be extracted.
3. If you don't have JavaFX lib, you can download it from [here][14]. Unzip the folder and place it to somewhere in your computer. Record the path for later use.
4. If you need to download JavaFX lib or if your javafx-sdk is not v11.0.2, you may need to re-build the path in Eclipse. Right click the project name "Citation-Graph-Java" in Eclipse, and then `Build Path` -> `Configure Build Path...`. Add libraries to Classpath, including JavaFX SDK as well as other JavaFX jar files downloaded.

-----------------------------------------

**Details:**

If you are using Mendeley (or any other Reference Management Software), export your papers as a **.bib** file which should include the arXiv ID and issue year information. Below example is a list of papers in Mendeley about adversarial examples, and we are going to study how they are related to each other (“citationally”).

![Image 1 - Mendeley][5]
**Image 1: Papers to be exported from Mendeley**

 Run `executable.jar` by following the above procedures, and a JavaFX GUI will pop up. Then import the .bib file by hitting the button on the tab "Input", and the papers' details will then be shown on the tab "Table". You can add papers by entering its key information and remove papers by ID. You can also edit the paper details by double clicking the text. There is a field of "nickname" which is the key of the .bib file, and you can switch between the nickname and the full title by checking the corresponding radio button.

![Image 2 - Import .bib gif][11]
**Image 2: Import .bib file at tab "Input", and add/remove/edit papers**

Click the web scrapping button at the bottom of the tab "Table", and a Chrome will be pop up for web scrapping. It will take you to the [Astrophysics Data System of Harvard][4] and find out the list of reference for each paper. It will take about 3 to 5 seconds to scrap one paper. Only paper with ArXiv ID will be scrapped.

![Image 3 - Web Scrapping gif][12]
**Image 3: Web scrapping of papers' citational relationship from database at tab "Table"**

After the web scrapping, the column "References" will be filled up, corresponding to the references information stored in the database. You can choose to show either the BibCode or the ID in the table.

To draw citation graph, go to tab "Graph" and hit the "Generate / Refresh" button. The citation graph will be pop up. You can choose different drawing option at the right of tab "Graph".

![Image 4 - Draw Graph gif][13]
**Image 4: Plot citation graphs at tab "Graph" with different setting/selection**

You can see that *24. Transferability in Machine Learning: from Phenomena to Black-Box Attacks using Adversarial Samples (Papernot et. al. 2016)* and *25. Explaining and harnessing adversarial examples (Goodfellow et. al. 2014)* are the most influential nodes among those selected papers (i.e. most cited), according to the citation relationship available in the database.

![Image 5 - Sample Citation Graph][1]
**Image 5: Sample output of citation graph**

If you want to filter papers by year range, or to show ancestor of a specific paper only, you can enter those information at the right of tab "Graph" as well.

---------------------------

**References:**
- [Wikipedia: Directed Acyclic Graph][7]

[1]: https://github.com/lanstonchu/Citation-Graph-Java/blob/master/GUI_Screenshots/Citation_Graph_Output_Sample.PNG
[2]: https://github.com/lanstonchu/citation-graph
[3]: https://github.com/lanstonchu/Citation-Graph-Python
[4]: https://ui.adsabs.harvard.edu/
[5]: https://raw.githubusercontent.com/lanstonchu/citation-graph/master/Mendeley.png
[6]: https://chromedriver.chromium.org/downloads
[7]: https://en.wikipedia.org/wiki/Directed_acyclic_graph
[8]: https://mvnrepository.com/artifact/org.jbibtex/jbibtex/1.0.17
[9]: https://www.selenium.dev/downloads/
[10]: https://jgrapht.org/
[11]: https://github.com/lanstonchu/Citation-Graph-Java/blob/master/GUI_Screenshots/Import_Bib.gif
[12]: https://github.com/lanstonchu/Citation-Graph-Java/blob/master/GUI_Screenshots/Citation_Web_Scrapping.gif
[13]: https://github.com/lanstonchu/Citation-Graph-Java/blob/master/GUI_Screenshots/Draw_Citation_Graph.gif
[14]: https://gluonhq.com/products/javafx/
