package cl.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import cl.batch.entities.Student;
import cl.batch.repository.StudentRepository;
import lombok.RequiredArgsConstructor;

//Indica que esta clase es una configuración de Spring y crea beans que pueden ser utilizados por el contenedor de Spring.
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

	// Inyecta automáticamente las dependencias necesarias mediante el constructor.
	private final JobRepository jobRepository; // Repositorio de trabajos de Spring Batch para gestionar los trabajos y
												// sus estados.
	private final PlatformTransactionManager platformTransactionManager; // Gestor de transacciones para manejar las
																			// transacciones de los pasos.
	private final StudentRepository repository; // Repositorio de JPA para la entidad Student.

	private static final String DEFAULT_DELIMITER = ",";

	private static final String ID_HEADER = "Id";
	private static final String FIRST_NAME_HEADER = "firstName";
	private static final String LAST_NAME_HEADER = "lastName";
	private static final String AGE_HEADER = "age";
	  
	// Define un bean para leer los datos desde un archivo CSV.
	@Bean
	public FlatFileItemReader<Student> reader() {
		FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
		// Especifica el archivo CSV de donde se leerán los datos.
		itemReader.setResource(new FileSystemResource("src/main/resources/students.csv"));
		itemReader.setName("csvReader");
		// Salta la primera línea del archivo (usualmente el encabezado).
		itemReader.setLinesToSkip(1);
		// Asigna el lineMapper para mapear cada línea del archivo a un objeto Student.
		itemReader.setLineMapper(lineMapper());
		return itemReader;
	}

	// Define un bean para procesar cada objeto Student leído.
	@Bean
	public StudentProcessor processor() {
		return new StudentProcessor();
	}

	// Define un bean para escribir los datos en el repositorio JPA.
	@Bean
	public RepositoryItemWriter<Student> writer() {
		RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
		// Establece el repositorio donde se guardarán los datos.
		writer.setRepository(repository);
		// Método del repositorio que se usará para guardar los datos.
		writer.setMethodName("save");
		return writer;
	}

	// Define un bean para un paso del trabajo que lee, procesa y escribe los datos.
	@Bean
	public Step step1() {
		return new StepBuilder("csvImport", jobRepository)
				// Define el tamaño del chunk y el gestor de transacciones.
				.<Student, Student>chunk(1000, platformTransactionManager).reader(reader()) // Asigna el lector.
				.processor(processor()) // Asigna el procesador.
				.writer(writer()) // Asigna el escritor.
				.taskExecutor(taskExecutor()) // Asigna el ejecutor de tareas para la ejecución paralela.
				.build();
	}

	// Define un bean para el trabajo Batch.
	@Bean
	public Job runJob() {
		return new JobBuilder("importStudents", jobRepository).start(step1()) // Define el primer paso del trabajo.
				.build();
	}

	// Define un bean para el ejecutor de tareas, permitiendo la ejecución paralela.
	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
		// Establece el límite de concurrencia para la ejecución paralela.
		asyncTaskExecutor.setConcurrencyLimit(10);
		return asyncTaskExecutor;
	}

	// Define el lineMapper para mapear cada línea del archivo CSV a un objeto
	// Student.
	private LineMapper<Student> lineMapper() {
		DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();

		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		// Establece la coma como delimitador.
		lineTokenizer.setDelimiter(DEFAULT_DELIMITER);
		// Desactiva el modo estricto.
		lineTokenizer.setStrict(false);
		// Establece los nombres de los campos en el archivo CSV.
		lineTokenizer.setNames(ID_HEADER, FIRST_NAME_HEADER, LAST_NAME_HEADER, AGE_HEADER);
		
		BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		// Establece el tipo de objeto al que se mapearán los datos.
		fieldSetMapper.setTargetType(Student.class);

		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		return lineMapper;
	}
}