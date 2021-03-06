
package Personal.CoursePlan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Notification.NotificationManager;

public class CoursePlanServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setHeader("content-type","text/html;charset=UTF-8");
		response.setHeader("Cache-Control","max-age=0");
		response.setContentType("application/json");
		
		CoursePlanManager coursePlanManager = new CoursePlanManager();
		ArrayList<CoursePlan> coursePlans = null;
		HttpSession session = request.getSession();
		String userId = (String)session.getAttribute("userId");
		
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.setPrettyPrinting().create();
		
		if(request.getParameter("action").equals("getVideoList")) {
			coursePlans = coursePlanManager.getVideoList(userId);
			response.getWriter().write(gson.toJson(coursePlans));
		}
		else if(request.getParameter("action").equals("getUnit")) {
			coursePlans = coursePlanManager.getCoursePlanUnit(userId
					,Integer.parseInt(request.getParameter("courselistId")));
			response.getWriter().write(gson.toJson(coursePlans));
		}
		else if(request.getParameter("action").equals("getAllUnit")) {
			coursePlans = coursePlanManager.getAllUnit(userId);
			response.getWriter().write(gson.toJson(coursePlans));
		}
		
		//關閉connection
		coursePlanManager.conClose();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CoursePlanManager manager = new CoursePlanManager();
		response.setHeader("Cache-Control","max-age=0");
		ArrayList<CoursePlan> coursePlans = null;
		ArrayList<CoursePlan> oldCoursePlans = null;
		CoursePlan coursePlan = new CoursePlan();
		HttpSession session = request.getSession();
		String userId = (String)session.getAttribute("userId");
		String action = request.getParameter("action");
		
		//紀錄排序
		if(action.equals("sortable")) {
			////////////////////////////更新移動後的清單的排序/////////////////////////////////////////////
			coursePlans = manager.getCoursePlanOrder(userId,request.getParameter("received"));
			//將原本那個狀態列表的資料都抓出來存在ArrayList
			for(int i = 0;i < coursePlans.size();i++) {
				if(coursePlans.get(i).getOorder() >= Integer.parseInt(request.getParameter("newIndex"))) {
					coursePlans.get(i).setOorder(coursePlans.get(i).getOorder() + 1);
				}
			}
			//將被移動的item的屬性塞進coursePlan
			coursePlan.setUserId(userId);
			coursePlan.setUnitId(Integer.parseInt(request.getParameter("unitId")));
			if(request.getParameter("received").equals("wantList"))coursePlan.setStatus(1);
			else if(request.getParameter("received").equals("ingList"))coursePlan.setStatus(2);
			else if(request.getParameter("received").equals("doneList"))coursePlan.setStatus(3);
			coursePlan.setOorder(Integer.parseInt(request.getParameter("newIndex")));
			//並且塞進coursePlans這個arrayList
			coursePlans.add(coursePlan);
			//更新資料
			for(int i = 0;i < coursePlans.size();i++) {
				manager.updateCoursePlanList(coursePlans.get(i));
			}
			/////////////////////////////////////////////////////////////////////////////////////////
			
			
			
			////////////////////////////更新移動前的清單的排序/////////////////////////////////////////////
			//將原本舊的狀態列表的資料都抓出來存在ArrayList
			oldCoursePlans = manager.getOldCoursePlanOrder(userId,request.getParameter("sender"));
			for(int i = 0;i < oldCoursePlans.size();i++) {
				if(oldCoursePlans.get(i).getOorder() > Integer.parseInt(request.getParameter("oldIndex"))) {
					oldCoursePlans.get(i).setOorder(oldCoursePlans.get(i).getOorder()-1);
					
				}
			}
			//更新資料
			for(int i = 0;i < oldCoursePlans.size();i++) {
				manager.updateOldStatusList(oldCoursePlans.get(i));
			}
			////////////////////////////////////////////////////////////////////////////////////////
			
		}
		else if(action.equals("deleteVideo")) {
			manager.deleteVideo(userId, Integer.parseInt(request.getParameter("unitId")));
		}
		//分享個人課程計畫至指定群組
		else if(action.equals("shareCoursePlanToGroup")) {
			NotificationManager notificationmanager = new NotificationManager();
			ArrayList<String> toUserIdList = new ArrayList<String>();
			toUserIdList = notificationmanager.getGroupUsers(
					Integer.parseInt(request.getParameter("groupId")), userId);
			
			manager.shareCoursePlanToGroup(toUserIdList,userId,Integer.parseInt(request.getParameter("unitId"))
					,Integer.parseInt(request.getParameter("groupId")));
		}
		//關閉connection
		manager.conClose();
	}
}
