package com.aura.smartschool.controller;

import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aura.smartschool.domain.ActivityVO;
import com.aura.smartschool.domain.AreaVO;
import com.aura.smartschool.domain.BoardVO;
import com.aura.smartschool.domain.BodyMeasureGrade;
import com.aura.smartschool.domain.BodyMeasureSummary;
import com.aura.smartschool.domain.ConsultHistoryVO;
import com.aura.smartschool.domain.ConsultVO;
import com.aura.smartschool.domain.HomeVO;
import com.aura.smartschool.domain.LocationVO;
import com.aura.smartschool.domain.MeasureItem;
import com.aura.smartschool.domain.MemberVO;
import com.aura.smartschool.domain.NotiVO;
import com.aura.smartschool.domain.OsInfoVO;
import com.aura.smartschool.domain.PayVO;
import com.aura.smartschool.domain.SchoolNotiVO;
import com.aura.smartschool.domain.SchoolVO;
import com.aura.smartschool.domain.SearchVO;
import com.aura.smartschool.domain.SessionVO;
import com.aura.smartschool.domain.VideoTimeVO;
import com.aura.smartschool.domain.VideoTypeVO;
import com.aura.smartschool.domain.VideoVO;
import com.aura.smartschool.result.Result;
import com.aura.smartschool.result.ResultData;
import com.aura.smartschool.service.MobileService;
import com.aura.smartschool.util.NetworkUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@RestController
public class ApiController {
	
	private static Logger logger = LoggerFactory.getLogger(ApiController.class);
	
	@Autowired
	private MobileService mobileService;
    
	//로그인하기 (모바일 home_id, mdn)
	@RequestMapping("/api/signInOfMobile")
    public ResultData<List<MemberVO>> signInOfMobile(@RequestBody MemberVO member) {
		logger.debug("/api/signInOfMobile--------------------------------------------------------");
		
		//home_id 존재하는지 체크, 
		MemberVO myInfo = mobileService.signInOfMobile(member);
		//존재한다면 구성원 리스트를 리턴
		if(myInfo != null) {
			HomeVO home = new HomeVO();
			home.setHome_id(member.getHome_id());
			List<MemberVO> memberList = mobileService.getMemberList(home); 
			ResultData<List<MemberVO>> result = new ResultData<List<MemberVO>>(0, "success", memberList);
			//reg id update
			mobileService.updateGcmId(member);
			
			return result;
		} else {
			return new ResultData<List<MemberVO>>(100, "login failed", null);
		}
    }
	
	//로그인하기  (웹 home_id, name)
	@RequestMapping("/api/signInOfWeb")
    public ResultData<List<MemberVO>> signInOfWeb(@RequestBody MemberVO member) {
		logger.debug("/api/signInOfWeb-----------------------------------------------------------");
		
		//home_id 존재하는지 체크, 
		MemberVO myInfo = mobileService.signInOfWeb(member);
		//존재한다면 구성원 리스트를 리턴
		if(myInfo != null) {
			HomeVO home = new HomeVO();
			home.setHome_id(member.getHome_id());
			List<MemberVO> memberList = mobileService.getMemberList(home); 
			ResultData<List<MemberVO>> result = new ResultData<List<MemberVO>>(0, "success", memberList);
			
			return result;
		} else {
			return new ResultData<List<MemberVO>>(100, "login failed", null);
		}
    }
	
	//회원가입 (모바일)
	@Transactional
	@RequestMapping("/api/signUp")
    public Result signUp(@RequestBody MemberVO member) {
		logger.debug("/api/signUp----------------------------------------------------------------");
		
		int result = 0;
		String msg = "success";
		try { 
			HomeVO home = new HomeVO();
			home.setHome_id(member.getHome_id());
			
			//home_id 가 존재하는지 체크
			if(mobileService.countHome(home) > 0) {
				result = 100;
				msg = "가족명이 이미 존재합니다";
			} else {
				//멤버 전화번호 중복 체크후 중복이 없을시 홈아이디와 멤버 생성
				if(mobileService.checkMemberExistInHome(member) > 0) {
					return new Result(100, "해당 전화번호는 이미 등록된 가족명을 가지고 있습니다.");
				} else {
					//홈아이디 생성
					mobileService.addHome(home);
					//멤버 생성
					mobileService.insertMember(member);

					return new Result(0, "success");
				}
			}
		} catch (PersistenceException e) {
			logger.debug("PersistenceException:");
			result = 500;
			msg = "server error";
		}
		
		return new Result(result, msg);
    }
	
	//가족 멤버 등록
	@RequestMapping("/api/addMember")
    public Result addMember(@RequestBody MemberVO member) {
		logger.debug("/api/addMember-------------------------------------------------------------");
		
		try {
			if(mobileService.checkMemberExistInHome(member) > 0) {
				return new Result(100, "중복된 전화번호가 존재하거나 가족명내에 동일한 이름이 존재합니다.");
			} else {
				long resultCount = mobileService.insertMember(member);
				if(resultCount > 0) {
					return new Result(0, "success");
				} else {
					return new Result(100, "insert failed");
				}
			}
		} catch (PersistenceException e) {
			return new Result(100, "insert failed");
		} 
	}
	
	//가족 멤버 수정
	@RequestMapping("/api/updateMember")
    public Result updateMember(@RequestBody MemberVO member) {
		logger.debug("/api/updateMember----------------------------------------------------------");
		
		if(mobileService.checkMemberExistInHome(member) > 1) {
			return new Result(100, "중복된 전화번호가 존재하거나 가족명내에 동일한 이름이 존재합니다.");
		} else {
			long resultCount = mobileService.updateMember(member);
			if(resultCount > 0) {
				return new Result(0, "success");
			} else {
				return new Result(100, "update failed");
			}
		}
	}
	
	//가족 멤버 삭제
	@RequestMapping("/api/removeMember")
    public Result removeMember(@RequestBody MemberVO member) {
		logger.debug("/api/removeMember----------------------------------------------------------");
		
		long resultCount = mobileService.removeMember(member);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "remove failed");
		}
	}
	
	//가족 멤버 리스트 가져오기
	@RequestMapping("/api/getMemberList")
    public Result getMemberList(@RequestBody HomeVO home) {
		logger.debug("/api/getMemberList---------------------------------------------------------");
		
		List<MemberVO> memberList = mobileService.getMemberList(home);
		
		if(memberList.size() > 0) {
			return new ResultData<List<MemberVO>>(0, "success", memberList);
		} else {
			return new ResultData<List<MemberVO>>(100, "home id does not exist", memberList);
		}
	}
	
	//홈아이디 변경하기
	@RequestMapping("/api/modifyHome")
    public Result modifyHome(@RequestBody SearchVO search) {
		logger.debug("/api/modifyHome------------------------------------------------------");
		
		HomeVO home = new HomeVO();
		home.setHome_id(search.getNew_home_id());
		int count = mobileService.countHome(home);
		
		if (count>0) {
			return new Result(100, "동일한 가족명이 존재합니다.");
		} else {
			search.setUse_yn(1);
			long resultCount = mobileService.modifyHome(search);
			if(resultCount > 0) {
				return new Result(0, "success");
			} else {
				return new Result(100, "remove failed");
			}
		}
	}
	
	//전화번호로 홈아이디 찾기
	@RequestMapping("/api/getHomeByNumber")
    public Result getHomeByNumber(@RequestBody MemberVO member) {
		logger.debug("/api/getHomeByNumber-------------------------------------------------------");
		
		HomeVO home = mobileService.getHomeListByNumber(member);
		
		if(home == null) {
			return new ResultData<HomeVO>(100, "등록된 가족명이 없습니다.", home);
		} else {
			return new ResultData<HomeVO>(0, "success", home);
		}
	}
	
	@RequestMapping("/api/getPayList")
    public Result getPayList(@RequestBody MemberVO member) {
		logger.debug("/api/getPayList---------------------------------------------------------");
		
		List<PayVO> payList = mobileService.getPayList(member);
		
		return new ResultData<List<PayVO>>(0, "success", payList);

	}
	
	@RequestMapping("/api/addPay")
    public Result addPay(@RequestBody PayVO pay) {
		logger.debug("/api/addPay---------------------------------------------------------");
		
		long result = mobileService.addPay(pay);
		
		return new Result(0, "success");
	}
	
	@RequestMapping("/api/modifyPay")
    public Result modifyPay(@RequestBody PayVO pay) {
		logger.debug("/api/modifyPay---------------------------------------------------------");
		
		long result = mobileService.modifyPay(pay);
		
		return new Result(0, "success");
	}
	
	@RequestMapping("/api/removePay")
    public Result removePay(@RequestBody PayVO pay) {
		logger.debug("/api/removePay---------------------------------------------------------");
		
		long result = mobileService.removePay(pay);
		
		return new Result(0, "success");
	}
	
	//자녀 위치 등록
	@RequestMapping("/api/addLocation")
    public Result addLocation(@RequestBody LocationVO location) {
		logger.debug("/api/addLocation-----------------------------------------------------------");
		
		int result = 0;
		String msg = "success";
		try {
			mobileService.insertLocation(location);
		} catch (PersistenceException e) {
			result = 500;
			msg = "server error";
		} 
		return new Result(result, msg);
	}
	
	//오늘날짜의 위치 데이터 가져오기
	@RequestMapping("/api/getLocationList")
    public Result getLocationList(@RequestBody MemberVO member) {
		logger.debug("/api/getLocationList-------------------------------------------------------");
		
		List<LocationVO> locationList = mobileService.selectLocationList(member);
		
		if(locationList.size() > 0) {
			return new ResultData<List<LocationVO>>(0, "success", locationList);
		} else {
			return new ResultData<List<LocationVO>>(100, "location does not exist", locationList);
		}
	}
	
	//오늘날짜의 가장 최근 위치 가져오기
	@RequestMapping("/api/getLastLocation")
	public Result getLastLocation(@RequestBody MemberVO member) {
		logger.debug("/api/getLastLocation-------------------------------------------------------");
		
		LocationVO location = mobileService.selectLastLocation(member);
		
		if(location == null) {
			return new ResultData<LocationVO>(100, "location does not exist", location);
		} else {
			return new ResultData<LocationVO>(0, "success", location);
		}
	}
	
	//학교 검색 API
	@RequestMapping("/api/getSchoolList")
	public Result getSchoolList(@RequestBody SchoolVO school) {
		logger.debug("/api/getSchoolList---------------------------------------------------------");
		
		List<SchoolVO> schoolList = mobileService.getSchoolList(school);
		
		if(schoolList.size() > 0) {
			return new ResultData<List<SchoolVO>>(0, "success", schoolList);
		} else {
			return new ResultData<List<SchoolVO>>(100, "school does not exist", schoolList);
		}
	}
	
	
	@RequestMapping("/api/addArea")
	public Result addArea(@RequestBody AreaVO area) {
		logger.debug("/api/addArea---------------------------------------------------------------");
		
		long result = mobileService.addArea(area);
		
		if(result > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "fail");
		}
	}
	
	@RequestMapping("/api/getArea")
	public Result getArea(@RequestBody AreaVO area) {
		logger.debug("/api/getArea---------------------------------------------------------------");
		
		AreaVO areaVO = mobileService.getArea(area);
		
		if(areaVO == null) {
			return new ResultData<AreaVO>(100, "area does not exist", areaVO);
		} else {
			return new ResultData<AreaVO>(0, "success", areaVO);
		}
	}
	
	@RequestMapping("/api/getAreaList")
    public Result getAreaList() {
		logger.debug("/api/getAreaList-----------------------------------------------------------");
		
		List<AreaVO> areaList = mobileService.getAreaList();
		
		if(areaList.size() > 0) {
			return new ResultData<List<AreaVO>>(0, "success", areaList);
		} else {
			return new ResultData<List<AreaVO>>(100, "area does not exist", areaList);
		}
	}
	
	
	//학생 신체정보 가져오기
	@RequestMapping("/api/getMeasureSummary")
	public Result getMeasureSummary(@RequestBody MemberVO member) {
		logger.debug("/api/getMeasureSummary-----------------------------------------------------");
		
		BodyMeasureSummary summary = mobileService.getSummary(member);
		if(summary != null) {
			return new ResultData<BodyMeasureSummary>(0, "success", summary);
		} else {
			return new ResultData<BodyMeasureSummary>(100, "data does not exist", summary);
		}
	}
	
	//학생 신체정보 가져오기
	@RequestMapping("/api/getHeight")
	public Result getHeight(@RequestBody MemberVO m) {
		logger.debug("/api/getHeight-----------------------------------------------------");
		ResultData<MeasureItem> result = new ResultData<MeasureItem>();
		
		MeasureItem vo = new MeasureItem();
		
		BodyMeasureGrade bodyMeasureGrade = mobileService.getMeasureGrade(m, "Height");
		if(bodyMeasureGrade != null) {
			vo.setMeasure_date(bodyMeasureGrade.getMeasureDate());
			vo.setValue(bodyMeasureGrade.getValue());
			vo.setBeforeValue(bodyMeasureGrade.getBeforeValue());
			vo.setGradeId(bodyMeasureGrade.getGradeId());
			vo.setGradeString(bodyMeasureGrade.getGradeDesc());

			//평균 세팅
			vo.setAverageOfSchool(bodyMeasureGrade.getAverageOfSchool());
			vo.setAverageOfLocal(bodyMeasureGrade.getAverageOfLocal());
			vo.setAverageOfNation(bodyMeasureGrade.getAverageOfNation());
			vo.setAverageOfStandard(bodyMeasureGrade.getAverageOfStandard());
			//랭킹 세팅
			vo.setRank(bodyMeasureGrade.getRank());
			vo.setBeforeRank(bodyMeasureGrade.getBeforeRank());
			vo.setTotal(bodyMeasureGrade.getTotal());
			vo.setBeforeTotal(bodyMeasureGrade.getBeforeTotal());
			
			result.setResult(0);
			result.setMsg("success");
			result.setData(vo);
		} else {
			
			result.setResult(100);
			result.setMsg("error");
		}
		
		return result;
	}
	
	@RequestMapping("/api/getWeight")
	public Result getWeight(@RequestBody MemberVO m) {
		logger.debug("/api/getWeight-----------------------------------------------------");
		ResultData<MeasureItem> result = new ResultData<MeasureItem>();
		
		MeasureItem vo = new MeasureItem();
		
		BodyMeasureGrade bodyMeasureGrade = mobileService.getMeasureGrade(m, "Weight");
		if(bodyMeasureGrade != null) {
			vo.setMeasure_date(bodyMeasureGrade.getMeasureDate());
			vo.setValue(bodyMeasureGrade.getValue());
			vo.setBeforeValue(bodyMeasureGrade.getBeforeValue());
			vo.setGradeId(bodyMeasureGrade.getGradeId());
			vo.setGradeString(bodyMeasureGrade.getGradeDesc());

			//평균 세팅
			vo.setAverageOfSchool(bodyMeasureGrade.getAverageOfSchool());
			vo.setAverageOfLocal(bodyMeasureGrade.getAverageOfLocal());
			vo.setAverageOfNation(bodyMeasureGrade.getAverageOfNation());
			vo.setAverageOfStandard(bodyMeasureGrade.getAverageOfStandard());
			//랭킹 세팅하기
			vo.setRank(bodyMeasureGrade.getRank());
			vo.setBeforeRank(bodyMeasureGrade.getBeforeRank());
			vo.setTotal(bodyMeasureGrade.getTotal());
			vo.setBeforeTotal(bodyMeasureGrade.getBeforeTotal());
			
			result.setResult(0);
			result.setMsg("success");
			result.setData(vo);
		} else {
			
			result.setResult(100);
			result.setMsg("error");
		}
		
		return result;
	}
	
	//학교 알리미 (공지사항, 가정통신문)
	//즐겨찾기 등록
	@RequestMapping("/api/addNotiBookmark")
    public Result addNotiBookmark(@RequestBody SchoolNotiVO inNoti) {
		logger.debug("/api/addNotiBookmark-------------------------------------------------------------");
		
		try {
			long resultCount = mobileService.addNotiBookmark(inNoti);
			if(resultCount > 0) {
				return new Result(0, "success");
			} else {
				return new Result(100, "insert failed");
			}
		} catch (PersistenceException e) {
			return new Result(100, "insert failed");
		} 
	}
	//즐겨찾기 삭제
	@RequestMapping("/api/removeNotiBookmark")
    public Result removeNotiBookmark(@RequestBody SchoolNotiVO inNoti) {
		logger.debug("/api/removeNotiBookmark-------------------------------------------------------------");
		
		try {
			long resultCount = mobileService.removeNotiBookmark(inNoti);
			if(resultCount > 0) {
				return new Result(0, "success");
			} else {
				return new Result(100, "insert failed");
			}
		} catch (PersistenceException e) {
			return new Result(100, "insert failed");
		} 
	}
	//공지사항, 가정통신문 가져오기 (모바일, 자기 학교, 카테고리에 해당하는 목록만 가져오기)
	@RequestMapping("/api/getSchoolNotiListByMember")
    public ResultData<List<SchoolNotiVO>> getSchoolNotiListByMember(@RequestBody SchoolNotiVO inNoti) {
		logger.debug("/api/getSchoolNotiListByMember--------------------------------------------------");
		
		List<SchoolNotiVO> notiList = mobileService.getSchoolNotiListByMember(inNoti);
		
		if(notiList == null) {
			return new ResultData<List<SchoolNotiVO>>(100, "data does not exist", null);
		} else {
			return new ResultData<List<SchoolNotiVO>>(0, "success", notiList);
		}
	}
	

	
	//실시간 상담====================================================================================
	@RequestMapping("/api/getConsultList")
    public ResultData<List<ConsultVO>> getConsultList(@RequestBody SessionVO inSession) {
		logger.debug("/api/getConsultList--------------------------------------------------");
		SessionVO session = mobileService.selectSession(inSession);
		
		if(session == null) {
			return new ResultData<List<ConsultVO>>(100, "data does not exist", null);
		} else {
			List<ConsultVO> consultList = mobileService.selectConsultList(session);

			return new ResultData<List<ConsultVO>>(0, "success", consultList);
		}
	}
	//상담 평가하고 종료하기
	@RequestMapping("/api/rateConsult")
    public Result rateConsult(@RequestBody SessionVO session) {
		logger.debug("/api/rateConsult----------------------------------------------------------");
		
		long resultCount = mobileService.updateSession(session);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	//상담 기록 가져오기(모바일, 학부모)
	@RequestMapping("/api/getConsultHistory")
    public Result getConsultHistory(@RequestBody SessionVO session) {
		logger.debug("/api/getConsultHistory---------------------------------------------------------");
		
		ConsultHistoryVO history = mobileService.getConsultHistory(session);
		
		return new ResultData<ConsultHistoryVO>(0, "success", history);

	}
	//상담 입력하기 (add session and consult)
	@RequestMapping("/api/addConsult")
    public Result addConsult(@RequestBody SessionVO inSession) {
		logger.debug("/api/addConsult----------------------------------------------------");
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
			MemberVO m = new MemberVO();
			m.setMember_id(inSession.getMember_id());
			MemberVO member = mobileService.selectMember(m);
			array.add(new JsonPrimitive(member.getGcm_id()));
			
			JsonObject data = new JsonObject();
			JsonObject value = new JsonObject();
			value.addProperty("category", inSession.getCategory());
			value.addProperty("content", inSession.getContent());
			data.addProperty("command", "consult");
			data.addProperty("value", value.toString());
			
			NetworkUtil.requestGCM(array, data);
		}
		
		return new Result(0, "success");
	}
	
	//앱 공지사항 가져오기=============================================================================
	@RequestMapping("/api/getNotiList")
    public ResultData<List<NotiVO>> getNotiList() {
		logger.debug("/api/getNotiList--------------------------------------------------");
		List<NotiVO> notiList = mobileService.getNotiList(new SearchVO());
		
		if(notiList == null) {
			return new ResultData<List<NotiVO>>(100, "data does not exist", null);
		} else {
			return new ResultData<List<NotiVO>>(0, "success", notiList);
		}
	}
	
	
	
	//학교 게시판(FaQ)==========================================================================
	@RequestMapping("/api/getBoardList")
    public ResultData<List<BoardVO>> getBoardList(@RequestBody SearchVO search) {
		logger.debug("/api/getBoardList--------------------------------------------------");
		List<BoardVO> boardList = mobileService.getBoardList(search);
		
		if(boardList == null) {
			return new ResultData<List<BoardVO>>(100, "data does not exist", null);
		} else {
			return new ResultData<List<BoardVO>>(0, "success", boardList);
		}
	}
	
	@RequestMapping("/api/addBoard")
    public Result addBoard(@RequestBody BoardVO inBoard) {
		logger.debug("/api/addBoard-------------------------------------------------------------");
		
		try {
			long resultCount = mobileService.addBoard(inBoard);
			if(resultCount > 0) {
				return new Result(0, "success");
			} else {
				return new Result(100, "insert failed");
			}
		} catch (PersistenceException e) {
			return new Result(100, "insert failed");
		} 
	}
	
	@RequestMapping("/api/modifyBoard")
    public Result modifyBoard(@RequestBody BoardVO inBoard) {
		logger.debug("/api/modifyBoard----------------------------------------------------------");
		
		long resultCount = mobileService.modifyBoard(inBoard);
		
		if(resultCount > 0) {
			//send gcm
			//check answer 
			if(inBoard.getAnswer() != null || inBoard.getAnswer() != "") {
				JsonArray array = new JsonArray(); //get gcm_id
				MemberVO member = mobileService.getBoardGcm(inBoard);
				array.add(new JsonPrimitive(member.getGcm_id()));
				
				JsonObject data = new JsonObject();
				JsonObject value = new JsonObject();
				value.addProperty("title", inBoard.getTitle());
				value.addProperty("content", inBoard.getContent());
				value.addProperty("answer", inBoard.getAnswer());
				
				data.addProperty("command", "qna");
				data.addProperty("value", value.toString());
				
				NetworkUtil.requestGCM(array, data);
			}
			
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/api/removeBoard")
    public Result removeBoard(@RequestBody BoardVO inBoard) {
		logger.debug("/api/removeBoard----------------------------------------------------------");
		
		long resultCount = mobileService.removeBoard(inBoard);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "delete failed");
		}
	}
	
	//활동량=====================================================================================
	@RequestMapping("/api/getActivityList")
    public ResultData<List<ActivityVO>> getActivityList(@RequestBody ActivityVO inActivity) {
		logger.debug("/api/getActivityList--------------------------------------------------");
		List<ActivityVO> activityList = mobileService.getActivityList(inActivity);
		
		return new ResultData<List<ActivityVO>>(0, "success", activityList);
	}
	
	//활동량을 update or insert 한다.
	@RequestMapping("/api/addActivity")
    public Result saveActivity(@RequestBody ActivityVO inActivity) {
		logger.debug("/api/addActivity-------------------------------------------------------------");
		
		ActivityVO activity = mobileService.getActivity(inActivity);
		long resultCount = 0;
		
		if(activity == null) {
			resultCount = mobileService.addActivity(inActivity);
		} else {
			inActivity.setActivity_id(activity.getActivity_id());;
			resultCount = mobileService.modifyActivity(inActivity);
		}
		
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "insert or update failed");
		}
	}
	
	//비디오리스트 가져오기: 키, 체중, BMI================================================================
	@RequestMapping("/api/getVideoListByMasterGradeId")
    public ResultData<List<VideoVO>> getVideoListByMasterGradeId(@RequestBody VideoTypeVO type) {
		logger.debug("/api/getVideoListByMasterGradeId--------------------------------------------------");
		List<VideoVO> videoList = mobileService.getVideoListByMasterGradeId(type);
		
		return new ResultData<List<VideoVO>>(0, "success", videoList);
	}
	//비디오리스트 가져오기: PT 화면
	@RequestMapping("/api/getVideoListByInfoType")
    public ResultData<List<VideoVO>> getVideoListByInfoType(@RequestBody VideoTypeVO type) {
		logger.debug("/api/getVideoListByInfoType--------------------------------------------------");
		List<VideoVO> videoList = mobileService.getVideoListByInfoType(type);
		
		return new ResultData<List<VideoVO>>(0, "success", videoList);
	}
	
	//비디오 최초 접속 시간 가져오기
	@RequestMapping("/api/getVideoTimeOfMember")
    public ResultData<VideoTimeVO> getVideoTimeOfMember(@RequestBody VideoTimeVO inVideoTime) {
		logger.debug("/api/getVideoTimeOfMember--------------------------------------------------");
		VideoTimeVO videoTime = mobileService.getVideoTimeOfMember(inVideoTime);
		
		if(videoTime == null) {
			mobileService.addVideoTime(inVideoTime);
			videoTime = mobileService.getVideoTimeOfMember(inVideoTime);
		}
		
		return new ResultData<VideoTimeVO>(0, "success", videoTime);
	}
	
	@RequestMapping("/api/getOsInfo")
    public ResultData<OsInfoVO> getVideoListByInfoType(@RequestBody OsInfoVO inOsInfo) {
		logger.debug("/api/getVideoListByInfoType--------------------------------------------------");
		OsInfoVO osInfo = mobileService.getOsInfo(inOsInfo);
		
		return new ResultData<OsInfoVO>(0, "success", osInfo);
	}
}
