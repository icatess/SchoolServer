package com.aura.smartschool.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aura.smartschool.domain.ConsultVO;
import com.aura.smartschool.domain.Member;
import com.aura.smartschool.domain.SchoolNoti;
import com.aura.smartschool.domain.SchoolVO;
import com.aura.smartschool.domain.SessionVO;
import com.aura.smartschool.result.Result;
import com.aura.smartschool.result.ResultData;
import com.aura.smartschool.result.ResultDataTotal;
import com.aura.smartschool.service.MobileService;
import com.aura.smartschool.util.NetworkUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@RestController
public class AdminController {
	private static Logger logger = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private MobileService mobileService;
    
	//get school list of member
	@RequestMapping("/admin/api/getSchoolListOfMember")
    public ResultData<List<SchoolVO>> getSchoolListOfMember(@RequestBody SchoolVO school) {
		logger.debug("/admin/api/getSchoolListOfMember-------------------------------------------");
		List<SchoolVO> schoolList = mobileService.getSchoolListOfMember(school);
		int total = mobileService.countSchoolListOfMember();
		
		return new ResultDataTotal<List<SchoolVO>>(0, "success", schoolList, total);
	}
	
	@RequestMapping("/admin/api/modifySchool")
    public Result updateSchool(@RequestBody SchoolVO school) {
		logger.debug("/admin/api/modifySchool----------------------------------------------------");
		
		long resultCount = mobileService.updateSchool(school);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/admin/api/addSchoolNoti")
    public Result addSchoolNoti(@RequestBody SchoolNoti noti) {
		logger.debug("/admin/api/addSchoolNoti---------------------------------------------------");
		long resultCount = mobileService.addSchoolNoti(noti);
		if(resultCount > 0) {
			//send gcm
			SchoolVO school = new SchoolVO();
			school.setSchool_id(noti.getSchool_id());;
			List<Member> memberList = mobileService.selectMemberOfSchool(school);
			JsonArray array = new JsonArray(); //get gcm_id
			for(Member m : memberList) {
				array.add(new JsonPrimitive(m.getGcm_id()));
			}
			
			JsonObject data = new JsonObject();
			data.addProperty("command", "school");
			data.addProperty("category", noti.getCategory());
			data.addProperty("title", noti.getTitle());
			data.addProperty("content", noti.getContent());
			data.addProperty("noti_date", noti.getNoti_date());
			
			NetworkUtil.requestGCM(array, data);
			
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/admin/api/modifySchoolNoti")
    public Result modifySchoolNoti(@RequestBody SchoolNoti noti) {
		logger.debug("/admin/api/modifySchoolNoti------------------------------------------------");
		long resultCount = mobileService.modifySchoolNoti(noti);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/admin/api/removeSchoolNoti")
    public Result removeSchoolNoti(@RequestBody SchoolNoti noti) {
		logger.debug("/admin/api/removeSchoolNoti------------------------------------------------");
		long resultCount = mobileService.removeSchoolNoti(noti);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/admin/api/getSchoolNotiList")
    public ResultData<List<SchoolNoti>> getSchoolNotiList(@RequestBody SchoolNoti noti) {
		logger.debug("/admin/api/getSchoolNotiList-----------------------------------------------");
		List<SchoolNoti> notiList = mobileService.getSchoolNotiList(noti);
		int total = mobileService.countSchoolNotiList(noti);
		return new ResultDataTotal<List<SchoolNoti>>(0, "success", notiList, total);
	}
	
	//add session and consult
	@RequestMapping("/admin/api/addConsult")
    public Result addConsult(@RequestBody SessionVO inSession) {
		logger.debug("/admin/api/addConsult----------------------------------------------------");
		SessionVO outSession = mobileService.selectSession(inSession);
		
		int session_id;
		if(outSession == null) {
			mobileService.insertSession(inSession);
			SessionVO session = mobileService.selectLastSession();
			session_id = session.getSession_id();
		} else {
			session_id = outSession.getSession_id();
		}
		ConsultVO consult = new ConsultVO();
		consult.setSession_id(session_id);
		consult.setContent(inSession.getContent());
		consult.setWho(inSession.getWho());
		mobileService.insertConsult(consult);
		
		//if who is 1, send gcm : member_id = > gcm id, content
		if(inSession.getWho() == 1) {
			JsonArray array = new JsonArray(); //get gcm_id
			Member m = new Member();
			m.setMember_id(inSession.getMember_id());
			Member member = mobileService.selectMember(m);
			array.add(new JsonPrimitive(member.getGcm_id()));
			
			JsonObject data = new JsonObject();
			data.addProperty("command", "consult");
			data.addProperty("category", inSession.getCategory());
			data.addProperty("content", inSession.getContent());
			
			NetworkUtil.requestGCM(array, data);
		}
		
		return new Result(0, "success");
	}
	
	//get current session
	@RequestMapping("/admin/api/getSessionList")
    public ResultData<List<SessionVO>> getSessionList(@RequestBody SessionVO inSession) {
		logger.debug("/admin/api/getSessionList--------------------------------------------------");
		List<SessionVO> sessionList = mobileService.selectSessionOngoingList(inSession);

		return new ResultData<List<SessionVO>>(0, "success", sessionList);
	}
	
	//get consult list
	@RequestMapping("/admin/api/getConsultList")
    public ResultData<List<ConsultVO>> getConsultList(@RequestBody SessionVO inSession) {
		logger.debug("/admin/api/getConsultList--------------------------------------------------");
		List<ConsultVO> consultList = mobileService.selectConsultList(inSession);

		return new ResultData<List<ConsultVO>>(0, "success", consultList);
	}
}
