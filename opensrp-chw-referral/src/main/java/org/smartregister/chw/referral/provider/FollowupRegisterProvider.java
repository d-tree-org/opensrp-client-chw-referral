package org.smartregister.chw.referral.provider;

import android.content.Context;
import android.database.Cursor;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.referral.R;
import org.smartregister.chw.referral.fragment.BaseReferralRegisterFragment;
import org.smartregister.chw.referral.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewProvider;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.text.MessageFormat;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.util.Utils.getName;

public class FollowupRegisterProvider implements RecyclerViewProvider<FollowupRegisterProvider.RegisterViewHolder> {

    protected static CommonPersonObjectClient client;
    private final LayoutInflater inflater;
    protected View.OnClickListener onClickListener;
    private View.OnClickListener paginationClickListener;
    private Context context;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    public FollowupRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.paginationClickListener = paginationClickListener;
        this.onClickListener = onClickListener;
        this.visibleColumns = visibleColumns;
        this.context = context;
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, final RegisterViewHolder viewHolder) {
        try {
            String fname = getName(
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true));
            String patientName = getName(fname, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true));

            String dobString = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
            int age = new Period(new DateTime(dobString), new DateTime()).getYears();

            viewHolder.patientName.setText(patientName + ", " + age);
            viewHolder.textViewVillage.setText(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, true));
            viewHolder.textViewGender.setText(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true));
            viewHolder.patientColumn.setOnClickListener(onClickListener);
            viewHolder.patientColumn.setTag(pc);
            viewHolder.patientColumn.setTag(R.id.VIEW_ID, BaseReferralRegisterFragment.CLICK_VIEW_NORMAL);

            viewHolder.registerColumns.setOnClickListener(onClickListener);
            viewHolder.dueButton.setOnClickListener(onClickListener);
            viewHolder.dueButton.setTag(pc);
            viewHolder.dueButton.setTag(R.id.VIEW_ID, BaseReferralRegisterFragment.FOLLOW_UP_VISIT);

            viewHolder.registerColumns.setOnClickListener(v -> viewHolder.dueButton.performClick());
            viewHolder.registerColumns.setOnClickListener(v -> viewHolder.patientColumn.performClick());

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, registerViewHolder);
        }
    }

    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, int currentPageCount, int totalPageCount, boolean hasNext, boolean hasPrevious) {
        FooterViewHolder holder = (FooterViewHolder) viewHolder;
        holder.nextPageView.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
        holder.pageInfoView.setText(MessageFormat.format(context.getString(org.smartregister.R.string.str_page_info), currentPageCount, totalPageCount));
        holder.previousPageView.setVisibility(hasPrevious ? View.VISIBLE : View.INVISIBLE);
        holder.previousPageView.setOnClickListener(paginationClickListener);
        holder.nextPageView.setOnClickListener(paginationClickListener);
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption filterOption, ServiceModeOption serviceModeOption, FilterOption filterOption1, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {
//        implement
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String s, String s1, String s2) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View v = inflater.inflate(R.layout.followup_register_list_row_item, parent, false);
        return new RegisterViewHolder(v);
    }

    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.smart_register_pagination, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public boolean isFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        return viewHolder instanceof FooterViewHolder;
    }

    class RegisterViewHolder extends RecyclerView.ViewHolder {
        public TextView patientName;
        public TextView textViewVillage;
        public Button dueButton;
        public View patientColumn;
        public View dueWrapper;
        public View registerColumns;
        public TextView textViewGender;

        public RegisterViewHolder(View itemView) {
            super(itemView);
            dueWrapper = itemView.findViewById(R.id.due_button_wrapper);
            patientColumn = itemView.findViewById(R.id.patient_column);
            textViewVillage = itemView.findViewById(R.id.text_view_village);
            textViewGender = itemView.findViewById(R.id.text_view_gender);
            dueButton = itemView.findViewById(R.id.due_button);
            patientName = itemView.findViewById(R.id.patient_name_age);
            registerColumns = itemView.findViewById(R.id.register_columns);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public Button previousPageView;
        public TextView pageInfoView;
        public Button nextPageView;

        FooterViewHolder(View view) {
            super(view);
            pageInfoView = view.findViewById(org.smartregister.R.id.txt_page_info);
            previousPageView = view.findViewById(org.smartregister.R.id.btn_previous_page);
            nextPageView = view.findViewById(org.smartregister.R.id.btn_next_page);
        }
    }
}
