package models;

import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@MappedSuperclass
public class SmartModel extends Model {
	@OneToMany
	public List<Log> logs;
}