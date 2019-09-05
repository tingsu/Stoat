import java.util.ArrayList;
import java.util.List;

//the layout class
public class ResLayout{
	//layout file name
	private String layoutFileName;
	public List<ResView> viewList = null;
	
	//constructor
	public ResLayout(String layoutFileName){
		this.layoutFileName = layoutFileName;
		viewList = new ArrayList<ResView>();
	}
	
	public void addResView(ResView rv){
		viewList.add(rv);
	}
	
	//getter
	public String getLayoutFileName(){
		return this.layoutFileName;
	}
	
}