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
import ru.practicum.ewm.main.compilation.model.CompilationEvent;
import ru.practicum.ewm.main.compilation.repository.CompilationEventRepository;
import ru.practicum.ewm.main.compilation.repository.CompilationRepository;
import ru.practicum.ewm.main.event.dto.EventShortDto;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationServiceImpl implements CompilationService {
    final CompilationRepository compilationRepository;
    final CompilationEventRepository compilationEventRepository;
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
        // поиск подборок по фильтру. запрос 1
        List<Compilation> allByPinned = compilationRepository.findAllByPinned(params.getPinned(), pageRequest);
        // получение данных о событиях. запрос 2
        List<CompilationEvent> compilationEvents = compilationEventRepository.findByCompilations(allByPinned.stream().map(Compilation::getId).toList());
        // группировка событий по подборкам
        Map<Compilation, List<CompilationEvent>> eventsByCompilations = compilationEvents.stream().collect(Collectors.groupingBy(CompilationEvent::getCompilation));

        List<CompilationDto> result = new ArrayList<>();

        for (Compilation compilation : allByPinned) {
            // dto-объект подборки
            CompilationDto compilationDto = modelMapper.map(compilation, CompilationDto.class);
            // события текущей подборки. если eventsByCompilations не содержит compilation, то возвращается пустой список
            List<Event> events = eventsByCompilations.getOrDefault(compilation, List.of()).stream().map(CompilationEvent::getEvent).toList();
            // маппинг Event -> EventShortDto
            compilationDto.setEvents(events.stream().map(event -> modelMapper.map(event, EventShortDto.class)).toList());
            result.add(compilationDto);
        }
        return result;
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
            compilation.setEvents(eventRepository.findByIdIn(patchCompilationDto.getEvents()));
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
