package ru.practicum.ewm.main.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.compilation.dto.CompilationDto;
import ru.practicum.ewm.main.compilation.dto.GetCompilationsParams;
import ru.practicum.ewm.main.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.main.compilation.dto.PatchCompilationDto;
import ru.practicum.ewm.main.compilation.model.Compilation;
import ru.practicum.ewm.main.compilation.repository.CompilationRepository;
import ru.practicum.ewm.main.event.dto.EventShortDto;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.repository.EventRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationServiceImpl implements CompilationService {
    final CompilationRepository compilationRepository;
    final EventRepository eventRepository;
    final ModelMapper modelMapper;

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = modelMapper.map(newCompilationDto, Compilation.class);
        if (compilation.getEvents() != null) {
            for (Long eventId : newCompilationDto.getEvents()) {
                compilation.getEvents().add(new Event(eventId));
            }
        }
        // сохранение подборки
        compilation = compilationRepository.save(compilation);
        return getCompilationDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow();
        return getCompilationDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(GetCompilationsParams params) {
        PageRequest pageRequest = PageRequest.of(params.getFrom(), params.getSize());
        return compilationRepository.findAll(pageRequest).stream()
                .map(compilation -> modelMapper.map(compilation, CompilationDto.class))
                .toList();
    }

    @Override
    public CompilationDto updateCompilation(PatchCompilationDto patchCompilationDto) {
        Compilation compilation = compilationRepository.findById(patchCompilationDto.getId()).orElseThrow();
        if (patchCompilationDto.getTitle() != null) {
            compilation.setTitle(patchCompilationDto.getTitle());
        }
        if (patchCompilationDto.getPinned() != null) {
            compilation.setPinned(patchCompilationDto.getPinned());
        }
        if (patchCompilationDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(patchCompilationDto.getEvents().stream().toList()));
        }
        compilation = compilationRepository.save(compilation);
        return modelMapper.map(compilation, CompilationDto.class);
    }

    @Override
    public void deleteCompilationById(long compId) {
        compilationRepository.deleteById(compId);
    }

    private CompilationDto getCompilationDto(Compilation compilation) {
        CompilationDto result = modelMapper.map(compilation, CompilationDto.class);
        List<Event> events = eventRepository.getEventsByCompilationId(compilation.getId());
        result.setEvents(events.stream().map(event -> modelMapper.map(event, EventShortDto.class)).toList());
        return result;
    }
}
