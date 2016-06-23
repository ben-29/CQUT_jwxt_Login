import org.apache.commons.lang3.StringUtils;

public class GradeInfo {
	//学年
	public String stuYear;
	//学期
	public String stuPeriod;	
	//课程名称
	public String className;
	//学分
	public String credit;
	//绩点
	public String point;
	//成绩
	public String grade;

	@Override
	public String toString() {
		if(StringUtils.isNumeric(grade)) //成绩 为 数字
			return stuYear + "\t" + stuPeriod + "\t" + grade + "\t\t"+ credit + "\t" + point + "\t" + className;
		else
			return stuYear + "\t" + stuPeriod + "\t" + grade + "\t"+ credit + "\t" + point + "\t" + className;
	}
}
