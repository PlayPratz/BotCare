package alpha.undefined.botcare;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private ArrayList<Message> mDataset;
	public MessageAdapter(ArrayList<Message> InputData) {
		mDataset = InputData;
	}


	public static class MessageViewHolder extends RecyclerView.ViewHolder {
		public TextView mText, mTime;

		public MessageViewHolder(ConstraintLayout c){
			super(c);
			mText = (TextView) c.findViewById(R.id.textMessage);
			mTime = (TextView) c.findViewById(R.id.textTime);
		}

		void bind(Message message) {
			mText.setText(message.getText());
			mTime.setText(message.getTime());
		}
	}

	public static class ImageViewHolder extends RecyclerView.ViewHolder {

		public ImageView mImage;
		public TextView mTime;

		public ImageViewHolder(ConstraintLayout c) {
			super(c);
			mImage = (ImageView) c.findViewById(R.id.imageMessage);
			mTime = (TextView) c.findViewById(R.id.textTime);
		}

		void bind (Message message) {
			mImage.setImageBitmap(message.getPhoto());
			mTime.setText(message.getTime());
		}
	}

	public static class DoctorViewHolder extends RecyclerView.ViewHolder {
		public TextView mName, mSpec, mCont;

		public DoctorViewHolder(ConstraintLayout c) {
			super(c);
			mName = (TextView) c.findViewById(R.id.text1);
			mSpec = (TextView) c.findViewById(R.id.text2);
			mCont = (TextView) c.findViewById(R.id.text3);
		}

		void bind (Message message) {
			mName.setText(message.getText());
			mSpec.setText(message.spec);
			mCont.setText(message.cont);
		}
	}


	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		RecyclerView.ViewHolder vh;
		ConstraintLayout constraintLayout;

		switch (viewType) {

			case ChatActivity.message_appointment:
				constraintLayout
						= (ConstraintLayout) LayoutInflater.from(parent.getContext())
						.inflate(R.layout.message_appointment, parent, false);
				vh = new DoctorViewHolder(constraintLayout);
				break;

			case ChatActivity.message_dose:
				constraintLayout
						= (ConstraintLayout) LayoutInflater.from(parent.getContext())
						.inflate(R.layout.message_dose, parent, false);
				vh = new DoctorViewHolder(constraintLayout);
				break;

			case ChatActivity.message_doctor:
				constraintLayout
						= (ConstraintLayout) LayoutInflater.from(parent.getContext())
						.inflate(R.layout.message_doctor, parent, false);
				vh = new DoctorViewHolder(constraintLayout);
				break;

			case ChatActivity.message_image_query:
				constraintLayout
						= (ConstraintLayout) LayoutInflater.from(parent.getContext())
						.inflate(R.layout.message_image_query, parent, false);
				vh = new ImageViewHolder(constraintLayout);
				break;

			case ChatActivity.message_response:
				constraintLayout
						= (ConstraintLayout) LayoutInflater.from(parent.getContext())
						.inflate(R.layout.message_response, parent, false);
				vh = new MessageViewHolder(constraintLayout);
				break;

			case ChatActivity.message_query:
			default:
				constraintLayout
						= (ConstraintLayout) LayoutInflater.from(parent.getContext())
						.inflate(R.layout.message_query, parent, false);
				vh = new MessageViewHolder(constraintLayout);
				break;
		}
		return vh;
	}



	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

		switch (getItemViewType(position)) {

			case ChatActivity.message_appointment:
				((DoctorViewHolder) holder).bind(mDataset.get(position));
				break;

			case ChatActivity.message_dose:
				((DoctorViewHolder) holder).bind(mDataset.get(position));
				break;

			case ChatActivity.message_doctor:
				((DoctorViewHolder) holder).bind(mDataset.get(position));
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_DIAL);
						intent.setData(Uri.parse("tel:"+mDataset.get(position).cont));
						v.getContext().startActivity(intent);
					}
				});
				break;

			case ChatActivity.message_image_query:
				((ImageViewHolder) holder).bind(mDataset.get(position));
				break;

			case ChatActivity.message_response:
				((MessageViewHolder) holder).bind(mDataset.get(position));
				break;

			case ChatActivity.message_query:
			default:
				((MessageViewHolder) holder).bind(mDataset.get(position));
				break;
		}

	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public int getItemViewType(int position) {
		return mDataset.get(position).getType();
	}
}
