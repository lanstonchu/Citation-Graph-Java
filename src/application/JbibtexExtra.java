package application;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXString;
import org.jbibtex.Key;
import org.jbibtex.ParseException;

/**
 * The JbibtexExtra class are some additional codes apart from the original Jbibtex library
 * These codes would be used to parse a .bib file 
 */
public class JbibtexExtra {

	static public BibTeXDatabase parseBibTeX(File file) throws IOException, ParseException {
		Reader reader = new FileReader(file);

		try {
			BibTeXParser parser = new BibTeXParser(){

				@Override
				public void checkStringResolution(Key key, BibTeXString string){

					if(string == null){
						System.err.println("Unresolved string: \"" + key.getValue() + "\"");
					}
				}

				@Override
				public void checkCrossReferenceResolution(Key key, BibTeXEntry entry){

					if(entry == null){
						System.err.println("Unresolved cross-reference: \"" + key.getValue() + "\"");
					}
				}
			};

			return parser.parse(reader);
		} finally {
			reader.close();
		}
	}

}
