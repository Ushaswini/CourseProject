﻿using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Web.Http.Description;
using Homework05.Models;

namespace Homework05.API_Controllers
{
    [Authorize(Roles ="Admin,StudyCoordinator")]
    [RoutePrefix("api/Questions")]
    public class QuestionsController : ApiController
    {
        private ApplicationDbContext db = new ApplicationDbContext();

        // GET: api/Questions
        public List<QuestionViewModel> GetQuestions()
        {
            var questions = db.Questions.ToList();
            var data = questions.Select(q => GetViewModel(q)).ToList();
            return data;
        }

        [Route("GetSurveyQuestions")]
        public List<QuestionViewModel> GetSurveyQuestions()
        {
            var questions = db.Questions.Where(q => q.QuestionType != QuestionType.Info).ToList();
            var data = questions.Select(q => GetViewModel(q)).ToList();
            return data;
        }

        private QuestionViewModel GetViewModel(Question question) {

            QuestionViewModel questionViewModel = new QuestionViewModel();

            questionViewModel.Id = question.Id;
            questionViewModel.QuestionText = question.QuestionText;
            questionViewModel.QuestionType = ((QuestionType)question.QuestionType).ToString();
            questionViewModel.Options = question.Options;
            questionViewModel.Minimum = question.Minimum;
            questionViewModel.Maximum = question.Maximum;
            questionViewModel.StepSize = question.StepSize;

            return questionViewModel;
        }

        // GET: api/Questions/5
        [ResponseType(typeof(Question))]
        public IHttpActionResult GetQuestion(int id)
        {
            Question question = db.Questions.Find(id);
            if (question == null)
            {
                return NotFound();
            }

            return Ok(question);
        }

        // PUT: api/Questions/5
        [ResponseType(typeof(void))]
        public IHttpActionResult PutQuestion(int id, Question question)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            if (id != question.Id)
            {
                return BadRequest();
            }

            db.Entry(question).State = EntityState.Modified;

            try
            {
                db.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!QuestionExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return StatusCode(HttpStatusCode.NoContent);
        }

        // POST: api/Questions
        [ResponseType(typeof(Question))]
        public IHttpActionResult PostQuestion(Question question)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            if (hasError(question))
            {
                return BadRequest(ModelState);
            }

            db.Questions.Add(question);

            try
            {
                db.SaveChanges();
            }
            catch (DbUpdateException)
            {
                if (QuestionExists(question.Id))
                {
                    return Conflict();
                }
                else
                {
                    throw;
                }
            }

            return CreatedAtRoute("DefaultApi", new { id = question.Id }, question);
        }

        private bool hasError(Question question) {

            bool hasError = false;

            switch (question.QuestionType.ToString()) {
                case "Scale":
                    if (question.Minimum < 0)
                    {
                        ModelState.AddModelError("Minimum", "Min should be greater than zero");
                        hasError = true;
                    }
                    if (question.Maximum < 0)
                    {
                        ModelState.AddModelError("Maximum", "Max should be greater than zero");
                        hasError = true;
                    }
                    if (question.Maximum < question.Minimum)
                    {
                        ModelState.AddModelError("StepSize", "Max should be greater than Min");
                        hasError = true;
                    }
                    if (question.StepSize < question.Minimum && question.StepSize > question.Maximum || question.StepSize == 0)
                    {
                        ModelState.AddModelError("StepSize", "Step Size should be in the range of Min and Max");
                        hasError = true;

                    }

                    if (question.StepSize != 0)
                    {
                        if (((question.Maximum - question.Minimum) / question.StepSize) > 10 || ((question.Maximum - question.Minimum) / question.StepSize) < 2)
                        {
                            ModelState.AddModelError("StepSize", "No of steps should be greater than 2 and less than 10");
                            hasError = true;
                        }

                        if (((question.Maximum - question.Minimum) % question.StepSize) != 0)
                        {
                            ModelState.AddModelError("StepSize", "Invalid stepsize");
                            hasError = true;
                        }
                    }


                    break;

                case "Choice":
                    if (question.Options == null || question.Options.Split(',').Length < 2)
                    {
                        ModelState.AddModelError("Options", "Min Two options should be present");
                        hasError = true;
                    }


                    break;
            }

            ModelState.AddModelError("","");


            return hasError;
        }

        // DELETE: api/Questions/5
        [ResponseType(typeof(Question))]
        public IHttpActionResult DeleteQuestion(string id)
        {
            Question question = db.Questions.Find(id);
            if (question == null)
            {
                return NotFound();
            }

            db.Questions.Remove(question);
            db.SaveChanges();

            return Ok(question);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool QuestionExists(int id)
        {
            return db.Questions.Count(e => e.Id == id) > 0;
        }
    }
}