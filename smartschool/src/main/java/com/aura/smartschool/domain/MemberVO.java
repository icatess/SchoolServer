package com.aura.smartschool.domain;

public class MemberVO {
	private String home_id;
	private int member_id;
	private String mdn;
	private String gcm_id;
	private int is_parent; //0:학생, 1:부모
	private String name;
	private String email;
	private String relation;
	private String photo;
	private int school_id;
	private String school_grade;
	private String school_class;
	private String sex;
	private String birth_date;
	private int use_yn = -1; //1:사용, 0: 삭제, -1:no assign
	private String access_date; //마지막접속날짜
	
	//school infomation
	private String school_name;
	private String address;
	private String new_address;
	private String lat;
	private String lng;
	private String homepage;
	private String contact;
	private String sido;
	private String gugun;
	private String gubun2;
	private String code;
	
	//결제일과 측정일
	private String pay_date;
	private String measure_date;
	
	//아우라홈페이지
	private String certifyKey;
	
	public String getHome_id() {
		return home_id;
	}
	public void setHome_id(String home_id) {
		this.home_id = home_id;
	}
	public int getMember_id() {
		return member_id;
	}
	public void setMember_id(int member_id) {
		this.member_id = member_id;
	}
	public String getMdn() {
		return mdn;
	}
	public void setMdn(String mdn) {
		this.mdn = mdn;
	}
	public String getGcm_id() {
		return gcm_id;
	}
	public void setGcm_id(String gcm_id) {
		this.gcm_id = gcm_id;
	}
	public int getIs_parent() {
		return is_parent;
	}
	public void setIs_parent(int is_parent) {
		this.is_parent = is_parent;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public int getSchool_id() {
		return school_id;
	}
	public void setSchool_id(int school_id) {
		this.school_id = school_id;
	}
	public String getSchool_grade() {
		return school_grade;
	}
	public void setSchool_grade(String school_grade) {
		this.school_grade = school_grade;
	}
	public String getSchool_class() {
		return school_class;
	}
	public void setSchool_class(String school_class) {
		this.school_class = school_class;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getBirth_date() {
		return birth_date;
	}
	public void setBirth_date(String birth_date) {
		this.birth_date = birth_date;
	}
	public String getSchool_name() {
		return school_name;
	}
	public void setSchool_name(String school_name) {
		this.school_name = school_name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getNew_address() {
		return new_address;
	}
	public void setNew_address(String new_address) {
		this.new_address = new_address;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = lng;
	}
	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getSido() {
		return sido;
	}
	public void setSido(String sido) {
		this.sido = sido;
	}
	public String getGugun() {
		return gugun;
	}
	public void setGugun(String gugun) {
		this.gugun = gugun;
	}
	public String getPay_date() {
		return pay_date;
	}
	public void setPay_date(String pay_date) {
		this.pay_date = pay_date;
	}
	public String getGubun2() {
		return gubun2;
	}
	public void setGubun2(String gubun2) {
		this.gubun2 = gubun2;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getUse_yn() {
		return use_yn;
	}
	public void setUse_yn(int use_yn) {
		this.use_yn = use_yn;
	}
	public String getAccess_date() {
		return access_date;
	}
	public void setAccess_date(String access_date) {
		this.access_date = access_date;
	}
	public String getMeasure_date() {
		return measure_date;
	}
	public void setMeasure_date(String measure_date) {
		this.measure_date = measure_date;
	}
	public String getCertifyKey() {
		return certifyKey;
	}
	public void setCertifyKey(String certifyKey) {
		this.certifyKey = certifyKey;
	}
	
}
