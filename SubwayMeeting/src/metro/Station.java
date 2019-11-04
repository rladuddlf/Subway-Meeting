package metro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Station {
  String stationCode;
   String stationName;
   ArrayList<String> LineList;
   String Line;
   ArrayList<String> externalCode;
   double latitude;
   double longitude;
   int stationCount;
   int stationValue;
   int transferValue;
   int transferAll;
   int reach;
   Queue<Station> Close;
   PreparedStatement pstmt;
   ResultSet rs;
   String sql;
   Connection cn;
   DBConnectionMgr db = DBConnectionMgr.getInstance();
   

   Station(String stationName, String tableName, Connection cn,int reach){
      this.reach = reach++;
   this.cn=cn;
     Close = new LinkedList<>();
     LineList = new ArrayList<>();
     //adCLose = new LinkedList<>();
     //System.out.println(stationName+ " " +Close + " �˶���������");
      this.stationName=stationName;
//      this.Line=Line;
//      this.externalCode=Integer.parseInt(externalCode);
      this.externalCode = new ArrayList<>();
      
      sql = "select * from "+tableName+" where name='"+stationName+"'";
      try {
       //  cn=db.getConnection();
         pstmt=cn.prepareStatement(sql);
         rs = pstmt.executeQuery();
         while(rs.next()) {
            stationCode = rs.getString(1);
            //Line.add(rs.getString(3));
            Line = rs.getString(3);
            LineList.add(rs.getString(3));
            externalCode.add(rs.getString(4));
            if(rs.getString(4).equals("K315"))   //���¿�. ���δ� P141�� �߰��ϰԵǸ� ���� ������ ���ܳ��� ���� ���� ���� ó����
               externalCode.add("P311");
            if(rs.getString(4).equals("548"))   //������. ���������̿��� �߰�
               externalCode.add("P548");
            latitude = rs.getDouble(5);
            longitude = rs.getDouble(6);
         }
      } catch (Exception e) {
            e.printStackTrace();
         }
      this.stationCount=0;
      this.stationValue=0;
      this.transferValue=0;
      this.transferAll=0;
   }
   Station(String stationName,Connection cn,int reach){
      this(stationName,"subway_info2",cn,reach);
   }
   Station(Station s,Connection cn){
      this(s.getName(),"subway_info2",cn,s.reach);
   }
   Station(int standard,Connection cn){
      this.stationCount=standard;
      this.stationValue=standard;
      this.transferValue=standard;
      this.transferAll=standard;
      this.cn=cn;
   }
   
   public void findClose() {
      Queue<String> queue = new LinkedList<>();
      Iterator<String> exIte = externalCode.iterator();
      Close.clear();
      while(exIte.hasNext()) {
        String str1;
        String str2;
        String str3;
         String nowString = (String) exIte.next();
         char ischar = nowString.charAt(0);
         if(nowString.contains("-")) {
            String front = nowString.split("-")[0].trim();
            String rear = nowString.split("-")[1].trim();
            int temp = Integer.parseInt(rear);
            if(temp == 1) {
               str1 = (temp+1)+"";
               str1 = front+"-"+str1;
               queue.add(str1);
               queue.add(front);
            }else {
               str1 = (temp+1)+"";
               str2 = (temp-1)+"";
               str1 = front+"-"+str1;
               str2 = front+"-"+str2;
               queue.add(str1);
               queue.add(str2);
            }
         }
         else if((ischar >= 65 && ischar <= 90) || (ischar >= 97 && ischar <= 122)) {
           String check = nowString;
            nowString = nowString.replace(ischar, ' ').trim();
            int temp = Integer.parseInt(nowString);
            str1 = (temp+1)+"";
            str2 = (temp-1)+"";
            str3 = (temp)+"-1";
            str1 = ischar+str1;
            str2 = ischar+str2;
            str3 = ischar+str3;
            if(check.equals("P142"))   //��������д��� -> ����
               str2 = "141";
            if(check.equals("P549"))   //���̵� -> ����
               str2 = "548";
            if(check.equals("P312"))   //����(�����߾Ӽ�) -> ����
               str2 = "K315";
            //System.out.println(temp);
            queue.add(str1);
            queue.add(str2);
            queue.add(str3);
            
         }else {
            int temp = Integer.parseInt(nowString);
            str1 = ""+(temp+1);
            str2 = ""+(temp-1);
            str3 = (temp)+"-1";
            if(temp == 615)   //����->����
               str1 = "610";
            if(temp == 610)   //����->����
               str2 = "616";
            if(temp == 616)//����->����
               str2 = "610";
            if(temp == 201)   //��û->������
               str2 = "243";
            if(temp == 243)   //������->��û
               str1 = "201";
            if(temp == 141)   //���� -> ��������д���
               str3 = "P142";
            queue.add(str1);
            queue.add(str2);
            queue.add(str3);
         }
   //   System.out.println(queue.poll()+"������");
      }
      
      while(!queue.isEmpty()) {
         String poll=queue.poll();
         sql = "select * from subway_info2 where externalCode='"+poll+"'";
         try {
          pstmt=cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if(rs.next()) {
               //System.out.println(rs.getString(2) + " Ȯ���ߵ�");
               //System.out.println(Close + "��");
               Close.add(new Station(rs.getString(2),cn,this.reach));
               //System.out.println(Close+ "��");
           //    System.out.println(Close.peek().getName()+"��������");
            }else
               continue;
         } catch (SQLException e) {
               e.printStackTrace();
         }
      }
   }
   
   public void findClose(Stack<Station> stack) {
         //ArrayList<Integer> intLine = new ArrayList<>();
         Queue<String> queue = new LinkedList<>();
         Stack<Station> s = new Stack<>();
         Close.clear();
         s.addAll(stack);
         Iterator<String> exIte = externalCode.iterator();
         while(exIte.hasNext()) {
           Iterator<Station> sIte = s.iterator();
           boolean F1 = true;
            boolean F2 = true;
            boolean F3 = true;
            String str1 = "";
            String str2 = "";
            String str3 = "";
            String nowString = (String) exIte.next();
            char ischar = nowString.charAt(0);
            if(nowString.contains("-")) {
               String front = nowString.split("-")[0].trim();
               String rear = nowString.split("-")[1].trim();
               int temp = Integer.parseInt(rear);
               if(temp == 1) {
                  str1 = (temp+1)+"";
                  str1 = front+"-"+str1;
                  str2 = front;
               }else {
                  str1 = (temp+1)+"";
                  str2 = (temp-1)+"";
                  str1 = front+"-"+str1;
                  str2 = front+"-"+str2;
               }
            }
            else if((ischar >= 65 && ischar <= 90) || (ischar >= 97 && ischar <= 122)) {
              String check = nowString;
               nowString = nowString.replace(ischar, ' ').trim();
               int temp = Integer.parseInt(nowString);
               str1 = (temp+1)+"";
               str2 = (temp-1)+"";
               str3 = (temp)+"-1";
               str1 = ischar+str1;
               str2 = ischar+str2;
               str3 = ischar+str3;
               if(check.equals("P142"))   //��������д��� -> ����
                  str2 = "141";
               if(check.equals("P549"))   //���̵� -> ����
                  str2 = "548";
               if(check.equals("P312"))   //����(�����߾Ӽ�) -> ����
                  str2 = "K315";
               //System.out.println(temp);    
            }else {
               int temp = Integer.parseInt(nowString);
               str1 = ""+(temp+1);
               str2 = ""+(temp-1);
               str3 = ""+(temp)+"-1";
               if(temp == 615)   //����->����
                  str1 = "610";
               if(temp == 610)   //����->����
                  str2 = "616";
               if(temp == 616)   //����->����
                  str2 = "610";
               if(temp == 201)   //��û->������
                  str2 = "243";
               if(temp == 243)   //������->��û
                  str1 = "201";
               if(temp == 141)   //���� -> ��������д���
                  str3 = "P142";
            }
               while(sIte.hasNext() && (F1||F2||F3)) {
                  Station compareS = (Station) sIte.next();
                  if(compareS.getExternalCode().contains(str1) && F1) {
                     //System.out.println(str1+"  �ߺ������ߺ������ߺ�����11111");
                     F1 = false;
                  }
                  if(compareS.getExternalCode().contains(str2) && F2) {
                     //System.out.println(str2+"  �ߺ������ߺ������ߺ�����222222");
                     F2 = false;
                  }
                  if(compareS.getExternalCode().contains(str3) && F3) {
                     //System.out.println(str2+"  �ߺ������ߺ������ߺ�����222222");
                     F3 = false;
                  }
               }
               if(F1 && !str1.isEmpty())
                  queue.add(str1);
               if(F2 && !str1.isEmpty())
                  queue.add(str2);
               if(F3 && !str1.isEmpty())
                  queue.add(str3);
      //   System.out.println(queue.poll()+"������");
         }
         
         //Iterator intLineIte = intLine.iterator();
         while(!queue.isEmpty()) {
            //System.out.println("�߾߾߾߾߾�");
            String poll=queue.poll();
            sql = "select * from subway_info2 where externalCode='"+poll+"'";
            try {
             pstmt=cn.prepareStatement(sql);
               rs = pstmt.executeQuery();
               if(rs.next()) {
                  //System.out.println(rs.getString(2) + " Ȯ���ߴ� ");
                  //System.out.println(Close + "��");
                  Close.add(new Station(rs.getString(2),cn,this.reach));//���
                  //System.out.println(Close + "��");
              //    System.out.println(Close.peek().getName()+"��������");
               }else
                  continue;
            } catch (SQLException e) {
                  e.printStackTrace();
            }
         }
      }
   public void Evalue(int count, int transfercount, int weight) {
      final int TRANSFER_WEIGHT = weight;
      int calValue = count+(transfercount*TRANSFER_WEIGHT);
      
      if(this.stationCount < count)
         this.stationCount = count;
      if(this.transferValue < transfercount)
         this.transferValue = transfercount;
      if(this.stationValue < calValue)
         this.stationValue = calValue;
      this.transferAll += transfercount;
   }
   public Queue<Station> getClose() {
      return this.Close;
   }
   public int adjustClose(Stack<Station> stack) {
      Queue<Station> aq = new LinkedList<>();
      while(!Close.isEmpty()) {
         Station temp = Close.poll();
         //System.out.println(temp.getName() + " ������ �̷��������");
         Stack<Station> s = new Stack<>();
         s.addAll(stack);
         while(!s.isEmpty()) {
            Station zzz = s.pop();
            //System.out.println(zzz.getName() + " �������� �񱳴�!!!");
            if(zzz.getName().equals(temp.getName())) {
               //System.out.println(temp.getName() + " ����� �̸��� �E�Կ� ");
               aq.addAll(Close);
               Close.addAll(aq);
               return 1;
            }
         }
         aq.add(temp);
      }
      return 0;
   }
   public String getLine() {
      return this.Line;
   }
   public String getStationCode() {
      return this.stationCode;
   }
   public String getName() {
      return this.stationName;
   }
   public ArrayList<String> getExternalCode() {
      return this.externalCode;
   }
   public double getLatitude() {
      return this.latitude;
   }
   public double getLongitude() {
      return this.longitude;
   }
   public int getValue() {
      return this.stationValue;
   }
   public int getCount() {
      return this.stationCount;
   }
   public int getTranserValue() {
      return this.transferValue;
   }
   public int getAll() {
      return this.transferAll;
   }
}