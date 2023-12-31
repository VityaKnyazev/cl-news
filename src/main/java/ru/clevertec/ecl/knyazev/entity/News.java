package ru.clevertec.ecl.knyazev.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "news")
public class News implements Serializable {

	private static final long serialVersionUID = -7619851169140124512L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "create_date_time", nullable = false)
	private LocalDateTime time;
	
	@Column(nullable = false, length = 100)
	private String title;
	
	@Column(name = "text_data", nullable = false)
	private String text;
	
	@Column(name = "author", nullable = false, length = 40)
	private String authorName;
	
	@OneToMany(mappedBy = "news", cascade = { CascadeType.REFRESH, CascadeType.REMOVE })
	private List<Comment> comments;
	
}
