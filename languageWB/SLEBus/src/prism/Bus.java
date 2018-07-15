package prism;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edit.Patch;

public class Bus {
	
	List<Stream> streams = new ArrayList<Stream>();
	
	public void createStream(Producer base, String streamId) {
		
		Optional<Stream> targetStream = 
				streams
				.stream() //Stream my streams
				.filter(stream -> stream.getId().equals(streamId))
				.findFirst();
		
		if(targetStream.isPresent()) {
			//TODO: error already exist
		}
		else {
			streams.add(new Stream(streamId,base));
		}
	}
	
	/**
	 * Subscribes 'cons' to published Patchs from 'streamId'
	 */
	public void subscribe(Consumer cons, String streamId) {
		
		Optional<Stream> targetStream = 
			streams
			.stream() //Stream my streams
			.filter(stream -> stream.getId().equals(streamId))
			.findFirst();
		
		if(targetStream.isPresent()) {
			targetStream.get().synchronize(cons);
		}
		else {
			//TODO: error stream not found?
		}
	}
	
	/**
	 * Publish 'p' on 'streamId'
	 */
	public void publish(Patch p, String streamId) {
		
//		System.out.println("Publish on "+streamId);
//		System.out.println(p);
		
		Optional<Stream> targetStream = 
				streams
				.stream() //Stream my streams
				.filter(stream -> stream.getId().equals(streamId))
				.findFirst();
		
		if(targetStream.isPresent()) {
			targetStream.get().push(p);
		}
		else {
			//TODO: error stream not found?
		}
	}
	
	public List<Stream> getStreams() {
		return streams;
	}
}
