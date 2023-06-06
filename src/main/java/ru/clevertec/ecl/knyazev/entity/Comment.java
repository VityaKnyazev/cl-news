package ru.clevertec.ecl.knyazev.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "comment")
public class Comment implements Serializable {

	private static final long serialVersionUID = -355263430030840600L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "create_date_time", nullable = false)
	private LocalDateTime time;
	
	@Column(name = "text_data", nullable = false, length = 800)
	private String text;
	
	@Column(name = "user_name", nullable = false, length = 35)
	private String userName;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "news_id")
	News news;
	
}
