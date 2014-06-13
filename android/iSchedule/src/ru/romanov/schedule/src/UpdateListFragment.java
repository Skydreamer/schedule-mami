package ru.romanov.schedule.src;

import java.util.ArrayList;

import ru.romanov.schedule.R;
import ru.romanov.schedule.adapters.ScheduleUpdateListAdapter;
import ru.romanov.schedule.adapters.SubjectAdapter;
import ru.romanov.schedule.utils.Subject;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

public class UpdateListFragment extends Fragment implements OnClickListener {
	ListView listView;
	
	SubjectAdapter subjectAdapter;
	ScheduleUpdateListAdapter updateListAdapter;
	
	ArrayList<Subject> subjects;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		subjectAdapter = new SubjectAdapter(getActivity());
        subjects =  subjectAdapter.getNewSubjects();
        updateListAdapter = new ScheduleUpdateListAdapter(getActivity(), subjects);
	}
	
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.update_list_layout, null);
    	listView = (ListView) view.findViewById(R.id.checkList);
    	Button confirmButton = (Button) view.findViewById(R.id.check_confirm_button);
    	
    	listView.setAdapter(updateListAdapter);
		confirmButton.setOnClickListener(this);
		return view;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.check_confirm_button:
			break;
		}
	}
}
