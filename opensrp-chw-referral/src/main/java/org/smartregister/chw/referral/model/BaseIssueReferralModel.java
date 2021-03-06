package org.smartregister.chw.referral.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.referral.ReferralLibrary;
import org.smartregister.chw.referral.domain.MemberObject;
import org.smartregister.chw.referral.domain.ReferralServiceIndicatorObject;
import org.smartregister.chw.referral.domain.ReferralServiceObject;
import org.smartregister.chw.referral.repository.ReferralServiceIndicatorRepository;
import org.smartregister.chw.referral.repository.ReferralServiceRepository;
import org.smartregister.chw.referral.util.DBConstants;
import org.smartregister.chw.referral.util.JsonFormUtils;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.Location;
import org.smartregister.repository.LocationRepository;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class BaseIssueReferralModel extends AbstractIssueReferralModel {
    @Override
    public String getLocationId(String locationName) {
        return null;
    }


    @Override
    public LiveData<List<Location>> getHealthFacilities() {
        try {
            LocationRepository locationRepository = new LocationRepository(ReferralLibrary.getInstance().getRepository());

            MutableLiveData<List<Location>> liveData = new MutableLiveData<>();
            liveData.setValue(locationRepository.getAllLocations());
            return liveData;
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    @Override
    public LiveData<List<ReferralServiceObject>> getReferralServicesList(List<String> referralServiceIds) {
        try {
            ReferralServiceRepository referralServiceRepository = new ReferralServiceRepository(ReferralLibrary.getInstance().getRepository());

            List<ReferralServiceObject> servicesList = new ArrayList<>();
            if (referralServiceIds != null) {
                for (String serviceId : referralServiceIds) {
                    try {
                        servicesList.add(referralServiceRepository.getReferralServiceById(serviceId));
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            } else {
                servicesList = referralServiceRepository.getReferralServices();
            }

            MutableLiveData<List<ReferralServiceObject>> liveData = new MutableLiveData<>();
            liveData.setValue(servicesList);

            return liveData;
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    @Override
    public List<ReferralServiceIndicatorObject> getIndicatorsByServiceId(String serviceId) {
        try {
            ReferralServiceIndicatorRepository indicatorRepository = new ReferralServiceIndicatorRepository(ReferralLibrary.getInstance().getRepository());
            return indicatorRepository.getServiceIndicatorsByServiceId(serviceId);
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    @Override
    public String mainSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.SelectInitiateMainTable(tableName, this.mainColumns(tableName));
        return queryBuilder.mainCondition(mainCondition);
    }

    protected String[] mainColumns(String tableName) {
        return new String[]{tableName + "." + DBConstants.KEY.RELATIONAL_ID, tableName + "." + DBConstants.KEY.BASE_ENTITY_ID, tableName + "." + DBConstants.KEY.FIRST_NAME, tableName + "." + DBConstants.KEY.MIDDLE_NAME, tableName + "." + DBConstants.KEY.LAST_NAME, tableName + "." + DBConstants.KEY.UNIQUE_ID, tableName + "." + DBConstants.KEY.GENDER, tableName + "." + DBConstants.KEY.DOB, tableName + "." + DBConstants.KEY.DOD};
    }

    @Override
    public JSONObject getFormWithValuesAsJson(String formName, String entityId, String currentLocationId, MemberObject memberObject) throws Exception {
        JSONObject jsonForm = JsonFormUtils.getFormAsJson(formName);
        JsonFormUtils.addFormMetadata(jsonForm, entityId, currentLocationId);

        return setFormValues(jsonForm, JsonFormConstants.STEP1, memberObject);
    }

    @VisibleForTesting
    public JSONObject setFormValues(JSONObject form, String step, MemberObject memberObject) {
        try {
            JSONArray fieldsArray = form.getJSONObject(step).getJSONArray(JsonFormConstants.FIELDS);
            setFieldValues(fieldsArray, memberObject);

            Timber.i("Form JSON = %s", form.toString());
        } catch (Exception e) {
            Timber.e(e);
        }
        return form;

    }

    private void setFieldValues(JSONArray fieldsArray, MemberObject memberObject) {

        JSONObject memberJSONObject = null;
        try {
            memberJSONObject = new JSONObject(new Gson().toJson(memberObject));
        } catch (JSONException e) {
            Timber.e(e);
        }
        for (int i = 0; i < fieldsArray.length(); i++) {
            JSONObject fieldObject;
            try {
                fieldObject = fieldsArray.getJSONObject(i);
                String key = fieldObject.getString(JsonFormConstants.KEY);
                if (memberJSONObject != null) {
                    fieldObject.put(JsonFormConstants.VALUE, memberJSONObject.getString(key));
                }
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }

}
