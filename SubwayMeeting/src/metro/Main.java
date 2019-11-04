package metro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
   static Connection cn=null;
   static DBConnectionMgr db = DBConnectionMgr.getInstance();
   static PreparedStatement pstmt;
   static ResultSet rs;
   static String sql;
   static Scanner scanner = new Scanner(System.in);
   static int num;
   static int[] weight;

   public static void main(String[] args) {
      try {
        cn = db.getConnection();
         setDB();
         count();
         closedStation();
         weight();
         //������������������������
         //num=3;
         //menu = new int[num];
         input();
         Meeting meeting = new Meeting(num, weight,cn);
         meeting.findCenter();
         meeting.loop();
         meeting.show();
   } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
   }
      finally {
          db.freeConnection(cn, pstmt, rs);
       }
      // TODO Auto-generated method stub
         return;
   }

   public static void input() {
      String stationCode = "";
      String name = "";
      String line = "";
      String externalCode = "";
      double latitude = 0;
      double longitude = 0;
      
      System.out.println("��߿��� �ڿ� �������ڸ� ���� �ܾ� ���̻��̿� ������ ������Ѵ�.");
      for (int i = 0; i < num; i++) {
         System.out.println(i + 1 + "��° �� �Է� ");
         name = scanner.nextLine();

         // ���� ����ó��
         String[] split = name.split(" ");
         if (split.length >= 2) {
            System.out.println("������ �ֽ��ϴ�. ���Է��ϼ���.");
            i--;
            continue;
         }
         
         //������������������������
         // ����ں� �޴� �Է�
         // System.out.println("�޴� �Է�");
         // System.out.println("1)�ּҰŸ� 2)�ּ�ȯ��");
         // menu[i] = scanner.nextInt();
         // if(menu[i] != 1 && menu[i] != 2) {
         // System.out.println("����� �Է� ����");
         // i--;
         // continue;
         // }
         // meeting[i] = new Meeting(menu);

         sql = "select * from subway_info2 where name='" + name + "'";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
               stationCode = rs.getString(1);
               name = rs.getString(2);
               line = rs.getString(3);
               externalCode = rs.getString(4);
               latitude = rs.getDouble(5);
               longitude = rs.getDouble(6);
               sql = "insert into departure values(?,?,?,?,?,?)";
               pstmt = cn.prepareStatement(sql);
               pstmt.setString(1, stationCode);
               pstmt.setString(2, name);
               pstmt.setString(3, line);
               pstmt.setString(4, externalCode);
               pstmt.setDouble(5, latitude);
               pstmt.setDouble(6, longitude);
               pstmt.executeUpdate();
            } else {
               System.out.println("�ش� ���� �����ϴ� ");
               i--;
            }
         } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }
   
   public static void weight() {
      //����ġ �߰� menu()���� weight�� �̸��� �ٲ�
      int temp = 0;
      int i=0;
      while(i<num) {
         System.out.println((i+1)+"��° ������� ȯ�� ����ġ�� �Է��ϼ���.");
            try{
               temp = scanner.nextInt();
               scanner.nextLine();
             }
             catch(InputMismatchException ime) {
                System.out.println("�ڿ����� �Է��ϼ���");
                System.exit(0);
             }
            if (temp < 1 || temp > 1000) {
                System.out.println("1~1000������ ���ڸ� �Է��ϼ���.");
             } else {
                weight[i] = temp;
                i++;
                System.out.println(i + "��° ����ڴ��� ȯ�� ����ġ�� " + temp + "�Դϴ�.");
             }
      }
   }

   public static void count() {
      while (true) {
         System.out.println("��߿��� �� �� �Դϱ� : ");
         try{
           num = scanner.nextInt();
           scanner.nextLine();
          }
          catch(InputMismatchException ime) {
             System.out.println("�ڿ����� �Է��ϼ���");
             System.exit(0);
          } 
         if (num < 3 || num > 5) {
            System.out.println("3~5������ ���ڸ� �Է��ϼ���.");
         } else {
            weight = new int[num];
           System.out.println("��߿��� " + num + "�� �Դϴ�." );
            break;
         }
      }
   }

   public static void closedStation() {
      int select=0;
      int number=0;
      String closedName="";
      String closedLine="";
      
      while (true) {
         System.out.println("1)���� ȣ�� 2)���� �� 3)��� ������ �����ϴ�.");
         try{
             select = scanner.nextInt();
             scanner.nextLine();
          }
          catch(InputMismatchException ime) {
             System.out.println("�ڿ����� �Է��ϼ���");
             System.exit(0);
          } 

         if (select == 2) {
            System.out.println(select+"���� �����ϼ̽��ϴ�.");
            System.out.println("��⿪�� ��ΰ���?");
            try{
                number = scanner.nextInt();
                scanner.nextLine();
             }
             catch(InputMismatchException ime) {
                System.out.println("�ڿ����� �Է��ϼ���");
                System.exit(0);
             }
            if(number==0)
               break;
            if(number < 0 || number >= 601) {
                System.out.println("0~600�� ���� ��⿪�� ������ �� �ֽ��ϴ�.");
                continue;
             }
            System.out.println("��⿪�� �� " + number + "�� �Դϴ�.");
            for (int i = 0; i < number; i++) {
               System.out.println("��⿪�� �Է����ּ���.�� '��'�ڴ� ���ֽð� ���⵵ ���������ּ���.");               
               closedName = scanner.nextLine();
               
               sql = "select name from subway_info2 where name='" + closedName + "'";
               try {
                  pstmt = cn.prepareStatement(sql);
                  rs = pstmt.executeQuery();
                  if (!rs.next()) {
                     System.out.println("�ش� ���� �����ϴ�.");
                     //System.out.println(closedName + "���� �����ϴ�.");
                     i--;
                     continue;
                  } else {
                     sql = "delete from subway_info2 where name='" + closedName + "'";
                     try {
                        pstmt = cn.prepareStatement(sql);
                        rs = pstmt.executeQuery();
                        System.out.println("����մϴ�.");
                        //System.out.println(closedName + "���� ����մϴ�.");
                     } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                     }
                  }
               } catch (SQLException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
            //break;
            
         } else if (select == 1) {
            System.out.println(select+"���� �����ϼ̽��ϴ�.");
            System.out.println("��� ȣ���� ��ΰ���?");
             try{
                 number = scanner.nextInt();
              }
              catch(InputMismatchException ime) {
                 System.out.println("�ڿ����� �Է��ϼ���.");
                 System.exit(0);
              }
             if(number==0)
                break;
             if(number < 0 || number >= 21) {
                System.out.println("0~20�� ���� ���ȣ���� ������ �� �ֽ��ϴ�.");
                continue;
             }
             System.out.println("��� ȣ���� �� "+ number + "�� �Դϴ�.");
             for (int i = 0; i < number; i++) {
                System.out.println("��� ȣ���� �Է����ּ���.(1~9, ����, �д�, �źд�, ��������, ����, ��õ1, ��õ2,"
                      + " �����߾�, �氭, ����, ������, ���̽ż�)");
                if(closedLine == "") {
                   closedLine = scanner.nextLine();
                }
                closedLine = scanner.nextLine();
                //System.out.println("closedLine = "+closedLine);
                closedLine = closedLine.trim();
                closedLine = closedLine.replaceAll(" ","");
                //System.out.println("closedLine = "+closedLine);
                
                switch(closedLine) {
                case "����":
                   closedLine="A";
                   break;
                case "�д�":
                   closedLine="B";
                   break;
                case "�źд�":
                   closedLine="S";
                   break;
                case "��������":
                   closedLine="E";
                   break;
                case "����":
                   closedLine="G";
                   break;
                case "��õ1":
                   closedLine="I";
                   break;
                case "��õ2":
                   closedLine="I2";
                   break;
                case "�����߾�":
                   closedLine="K";
                   break;
                case "�氭":
                   closedLine="KK";
                   break;
                case "����":
                   closedLine="SU";
                   break;
                case "������":
                   closedLine="U";
                   break;
                case "���̽ż�":
                   closedLine="UI";
                   break;
                }
                
                sql = "select name from subway_info2 where line='" + closedLine + "'";
                try {
                   pstmt = cn.prepareStatement(sql);
                   rs = pstmt.executeQuery();
                   if (!rs.next()) {
                       System.out.println("�ش� ȣ���� �����ϴ�.");
                      //System.out.println(closedLine + "ȣ���� �����ϴ�.");
                      i--;
                      continue;
                   } else {
                      sql = "delete from subway_info2 where line='" + closedLine + "'";
                      try {
                         pstmt = cn.prepareStatement(sql);
                         rs = pstmt.executeQuery();
                         //System.out.println("�ش� ȣ���� ����մϴ�");
                         System.out.println(closedLine + "ȣ���� ����մϴ�");
                      } catch (SQLException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                      }
                   }
                } catch (SQLException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
                }
             }
             //break;
         }
         else if(select==3)
            break;
         else {
            System.out.println("1, 2, 3 �� �ϳ��� �Է��ϼ���.");
         }
      }
   }

   public static void setDB() {  
         sql = "delete from departure where name is not null";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
         } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         sql = "delete from destination where name is not null";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
         } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         sql = "delete from scope where name is not null";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
         } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         sql = "delete from subway_info2 where name is not null";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
         } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
                  
         sql = "insert into subway_info2 select * from subway_info";
            try {
               pstmt = cn.prepareStatement(sql);
               rs = pstmt.executeQuery(sql);
            } catch (SQLException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
      }
   
   
}