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

public class Meeting {
   int[] weight;   //����ں� ����ġ
   int index;   //����� ��
   Station standard;   //���ؿ�
   String[] ans;   //��߿���
   String sql;

   PreparedStatement pstmt;
   ResultSet rs;
   Connection cn;
   DBConnectionMgr db = DBConnectionMgr.getInstance();

   Meeting(int index, int[] weight, Connection cn) {
      this.index = index;
      this.weight = weight;
      this.cn = cn;
      this.ans = new String[index];
      sql = "select * from departure";
      try {
         // cn = db.getConnection();
         pstmt = cn.prepareStatement(sql);
         rs = pstmt.executeQuery();
         int count = 0;
         while (rs.next()) {
            //System.out.println(rs.getString(2));
            ans[count] = rs.getString(2);
            // System.out.println(1234);

            count++;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      this.standard = new Station(9999, cn);   //���ؿ� 9999�� �ʱ�ȭ
   }

   public void findCenter() {
      double latitude = 0;
      double longitude = 0;
      double avgLatitude = 0;
      double avgLongitude = 0;

      sql = "select * from departure";

      try {
         pstmt = cn.prepareStatement(sql);
         rs = pstmt.executeQuery();
         // �����߽�(x,y) ���ϱ�
         for (int i = 0; i < index; i++) {
            if (rs.next()) {
               latitude += rs.getDouble(5);
               longitude += rs.getDouble(6);
            }
         }

         avgLatitude = latitude / index;
         avgLongitude = longitude / index;         

         double x1 = avgLatitude - 0.0225;// ���� 1���� �뷫 111km. ���� 1km�� �뷫 1/111=0.009. ���� 3km�� 0.009*3 = 0.027
         double x2 = avgLatitude + 0.0225;
         double y1 = avgLongitude - 0.0275;// �浵 1���� �뷫 88km. ���� 1km�� �뷫 1/88=0.011. ���� 3km�� 0.011*3 = 0.033
         double y2 = avgLongitude + 0.0275;

         // System.out.println(avgLatitude + ", " + avgLongitude);

         // +- 3km ���� ���ϱ�(���� �ȿ� ������ 1km�� ������) + Scope Table�� �ֱ�.
         sql = "delete from scope where name is not null";
         pstmt = cn.prepareStatement(sql);
         pstmt.executeQuery();
         while (true) {
            sql = "insert into scope select * from subway_info2 where latitude >" + x1 + " and latitude <" + x2
                  + " and longitude >" + y1 + "and longitude < " + y2;
            pstmt = cn.prepareStatement(sql);
            pstmt.executeQuery();

            sql = "select * from scope";
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
               x1 -= 0.009;
               x2 += 0.009;
               y1 -= 0.011;
               y2 += 0.011;
            } else
               break;
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   public Station findDestination(Station s) {
      Queue<Station> list = new LinkedList<>();
      Stack<Station> stack = new Stack<>();
      Station now;
      Station st;
      Station end;
      Station transferCheck;

      for (int i = 0; i < index; i++) {
         list.clear();
         stack.clear();
         transferCheck = null;
         int count = 0;
         int transfer = 0;
         list.add(s);

         while (list != null) {
//             Iterator si1 = stack.iterator();
//             Iterator sq1 = list.iterator();
//             System.out.print("Ȯ��Ȯ�ξ���� ���� : ");
//             while(si1.hasNext()) {
//                Station ts1 = (Station) si1.next();
//                System.out.print(ts1.getName() + " ");
//             }
//             System.out.println();
//             System.out.print("Ȯ��Ȯ�ξ���� ť : ");
//             while(sq1.hasNext()) {
//                Station tq1 = (Station) sq1.next();
//                System.out.print(tq1.getName() +"("+tq1.getLine()+")" + " ");
//             }
//             System.out.println();

            now = list.poll();
//           System.out.println(now.getName() + " ������ ");
//            try {
//            Thread.sleep(50);
//         } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//         }
            // System.out.println(ans[i] + " �� ã�Ƴ����� ����");
            
            if (now == null || now.reach > standard.getValue()) {
//               System.out.println(s.getName()+": �Ա���");
//               while(!stack.empty()) {
//                  System.out.print(stack.pop().getName()+" ");
//               }      System.out.println();
               return null;
            } else if (now.getName().equals(ans[i])) {
            //if (now.getName().equals(ans[i])) {
               // System.out.println("����߰�!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

               while (!stack.isEmpty()) {
                  st = new Station(now, cn);
                  end = stack.pop();
                  st.findClose();
                  while (!st.getClose().isEmpty()) {
                     if (st.getClose().poll().getName().equals(end.getName())) {
                        if(transferCheck != null) {
                           Iterator<String> ite = transferCheck.LineList.iterator();
                           while(ite.hasNext()) {
                              if(end.LineList.contains(ite.next()))
                                 break;
                              else if(!ite.hasNext()) {
                                 transfer++;
                                 break;
                              }
                           }
                        }
                        count++;
                        transferCheck = new Station(now, cn);
                        now = new Station(end, cn);
                        break;
                     }
                  }
               }
               break;
            } else {
            //adjustClose�� �۾��ϰ� �Ǹ� �ֺ����� ã���� �ѹ�, �ٽ� �ߺ������ϴ� �� �ѹ� �ؼ� �ι��� ���ؼ� ��ȿ����
//               now.findClose();
//               //���⼭ �������ΰŴ� ���ְ� �־����
//               if(!stack.isEmpty()) {
//                  now.adjustClose(stack);
//                  //System.out.println("������");
//               }
               //adjustClose�� ��ȿ���� ���� -> �ֺ��� ã���鼭 �ѹ��� ����
               if (!stack.isEmpty()) {
                  now.findClose(stack);
               } else {
                  now.findClose();
               }
               list.addAll(now.getClose());
               stack.push(now);

//               Iterator si = stack.iterator();
//               Iterator sq = list.iterator();
//               System.out.print("����� ���� : ");
//               while(si.hasNext()) {
//                  Station ts = (Station) si.next();
//                  System.out.print(ts.getName() + " ");
//               }
//               System.out.println();
//               System.out.print("����� ť : ");
//               while(sq.hasNext()) {
//                  Station tq = (Station) sq.next();
//                  System.out.print(tq.getName() +"("+tq.getLine()+")" + " ");
//               }
//               System.out.println();               
            }
         }
         s.Evalue(count, transfer, weight[i]);
         // System.out.println(s.getName()+"���� "+ ans[i] + "���� count : "+ count + "transfer : "+transfer);
         if(s.getValue() > standard.getValue())
            return null;
         else
            continue;
         //������������������������
         //���� getValue�� ���� �Ǽ� switch case �κ��� �ּ�ó��
//         switch (weight[i]) {
//         case 1:
//            if (s.getCount() > standard.getCount())
//               return null;
//            else
//               continue;
//         case 2:
//            if (s.getValue() > standard.getValue())
//               return null;
//            else
//               continue;
//         case 3:
//            if (s.getAll() > standard.getAll())
//               return null;
//            else {
//               if (s.getCount() > standard.getCount())
//                  return null;
//               else
//                  continue;
//            }
//         }
      }
      // System.out.println(s.getName()+"Ž�� !="+s.getCount());

      return s;
   }

   public void loop() {
      ArrayList<Station> scope = new ArrayList<>();
      sql = "select name from scope";
      try {
         pstmt = cn.prepareStatement(sql);
         rs = pstmt.executeQuery();
         while (rs.next()) {
            scope.add(new Station(rs.getString(1), "scope", cn,0));
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }

      Iterator<Station> ite = scope.iterator();
      while (ite.hasNext()) {
         Station s = findDestination((Station) ite.next());
//            if(s != null)
//               System.out.println(s.getName() + " " + s.getValue());
         if (s == null)
            continue;
         else if (s.getValue() < standard.getValue()) { // ���� getValue�� �ٲٸ� ȯ�¿������� ����Ѱ�
            sql = "delete from destination";
            try {
               pstmt = cn.prepareStatement(sql);
               rs = pstmt.executeQuery();
               sql = "insert into destination values(?,?,?,?,?,?)";
               pstmt = cn.prepareStatement(sql);
               pstmt.setString(1, s.getStationCode());
               pstmt.setString(2, s.getName());
               // pstmt.setString(3, s.getLine().get(0));
               pstmt.setString(3, s.getLine());
               pstmt.setString(4, s.getExternalCode().get(0));
               pstmt.setDouble(5, s.getLatitude());
               pstmt.setDouble(6, s.getLongitude());
               pstmt.executeUpdate();
               standard = s;
            } catch (SQLException e) {
               e.printStackTrace();
            }
         } else if (s.getValue() == standard.getValue()) {
            // }else {
            sql = "insert into destination values(?,?,?,?,?,?)";
            try {
               pstmt = cn.prepareStatement(sql);
               pstmt.setString(1, s.getStationCode());
               pstmt.setString(2, s.getName());
               // pstmt.setString(3, s.getLine().get(0));
               pstmt.setString(3, s.getLine());
               pstmt.setString(4, s.getExternalCode().get(0));
               pstmt.setDouble(5, s.getLatitude());
               pstmt.setDouble(6, s.getLongitude());
               pstmt.executeUpdate();
//                    pstmt=cn.prepareStatement(sql);
//                      rs = pstmt.executeQuery();
            } catch (SQLException e) {
               e.printStackTrace();
            }
         } else {
            continue;
         }
      }
   }

   public void show() {
      sql = "select * from destination";
      ArrayList<String> ans = new ArrayList<>();
      try {
         pstmt = cn.prepareStatement(sql);
         rs = pstmt.executeQuery();
         while (rs.next()) {
            String print = rs.getString(2);
            if (ans.isEmpty()) {
               System.out.println("���� ���� " + print + "��");
               ans.add(print);
               continue;
            } else {
               if (ans.contains(print)) {
                  break;
               } else {
                  ans.add(print);
                  System.out.println("���� ���� " + print + "��");
               }
            }
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }
}