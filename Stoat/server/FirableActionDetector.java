import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirableActionDetector{
	
	static List<String> listImages ;
    static List<String> listEditBoxes ;
    static List<String> listTextViewString ;
    static List<String> listButtonString ;
    static List<String> listTexts ;
    
    private static FirableActionDetector detector= new FirableActionDetector();
    private FirableActionDetector(){
    	listImages = new ArrayList<String>();
    	listEditBoxes = new ArrayList<String>();
    	listTextViewString = new ArrayList<String>();
    	listButtonString = new ArrayList<String>();
    	listTexts = new ArrayList<String>();
    }
    public static FirableActionDetector getDetector(){
    	return detector;
    }
    
    //detect firable actions
    public String detectFirableActions(String appStateFileName) throws FileNotFoundException, IOException
    {
        int imageBtnCount = 0;
        int imageViewCount = 0;
        int editTextCunt = 0;
        
        String firableActions ="";
        
        //open the app state file
        BufferedReader br = new BufferedReader(new FileReader(appStateFileName));
        
        String line;
        while ((line = br.readLine()) != null)
        {
        	
        	if(line.length() == 0)
        		break;
        	
            String trimmedLine = line.trim().replace("  ", " ");
            String[] objectNames = trimmedLine.split(" ");
            String objectName = objectNames[(objectNames.length - 1)];  //get the last object in the line
            
            if (objectName.contains(".")) {
                System.out.println(objectName + "-");
            }
            if (objectName.contains("ImageButton")) //is it an ImageButton?
            {
                listImages.add(objectName);
                firableActions += Action.imageBtnClick + "(" + (imageBtnCount + 1) + ")\n";
                imageBtnCount++;
            }
            else if (objectName.contains("ImageView")) //is it an ImageView?
            {
                listImages.add(objectName);
                firableActions += Action.imageViewClick + "(" + (imageViewCount + 1) + ")\n";
                imageViewCount++;
            }
            else if (objectName.contains("EditText")) //is it an EditText?
            {
                listEditBoxes.add(objectName);
                firableActions += Action.editText + "(" + editTextCunt + ", \"test\")\n";
                editTextCunt++;
            }
            else if (trimmedLine.contains("<:>")) //is it a TextView? (including TextView, Button)
            {
                if (trimmedLine.split("<:>").length > 1)
                {
                    String text = trimmedLine.split("<:>")[1];
                    listTexts.add(text);
                    if ((text.contains("\"")) || ((text.contains("(")) && (text.contains(")")))) {
                        text = text.replace("\"", "");
                        text = text.replace("(", "");
                        text = text.replace(")", "");
                    }
                    
                    firableActions += Action.textClick + "(\"" + text + "\")\n";
                }
            }
        }
        
        //close the file
        br.close();
        return firableActions;
    }
}

